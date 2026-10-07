package org.peopleshores.capstone_project.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/** Turns every exception into the documented JSON error body. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static String pathOf(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(InvalidCredentialsException ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage(), pathOf(request));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Raised by @PreAuthorize when an authenticated user lacks the required role.
     * Without this handler the catch-all below would report it as a 500, which is
     * both wrong and misleading during a security review.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied on {}", pathOf(request));
        ErrorResponse body = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), "Forbidden",
                "Your role does not permit this action.", pathOf(request));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), pathOf(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), pathOf(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), pathOf(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Database constraint violated", ex);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(), "Conflict",
                "The request conflicts with existing data.", pathOf(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Validation failed.", pathOf(request));

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        body.setValidationErrors(fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * The catch-all. The full root cause goes to the log, where operators can
     * read it; the caller gets a deliberately uninformative message, because
     * exception class names and internal messages are reconnaissance.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);

        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        log.error("Root cause on {}: {}: {}", pathOf(request), root.getClass().getName(), root.getMessage());

        String detail = "The request could not be completed. The error has been logged.";

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                detail, pathOf(request));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}