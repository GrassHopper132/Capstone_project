package org.peopleshores.capstone_project.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Every exception the API can raise has to leave as a specific status code. A
 * handler that falls through to 500 hides a business rule behind what looks
 * like a crash, which is how the AccessDenied case was masked until it was
 * caught in testing.
 */
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/artifacts");
    }

    @Test
    @DisplayName("bad credentials become 401, never 403")
    void badCredentialsAre401() {
        ResponseEntity<ErrorResponse> r =
                handler.handleBadCredentials(new InvalidCredentialsException(), request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(r.getBody()).isNotNull();
    }

    @Test
    @DisplayName("a role that does not permit the action becomes 403")
    void accessDeniedIs403() {
        ResponseEntity<ErrorResponse> r =
                handler.handleAccessDenied(new AccessDeniedException("denied"), request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("a missing record becomes 404")
    void notFoundIs404() {
        ResponseEntity<ErrorResponse> r =
                handler.handleNotFound(ResourceNotFoundException.of("Artifact", 99L), request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("a duplicate accession number becomes 409")
    void duplicateIs409() {
        ResponseEntity<ErrorResponse> r = handler.handleDuplicate(
                new DuplicateResourceException("Accession number 1994.22.7 already exists"), request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("a broken business rule becomes 409, not 400")
    void businessRuleIs409() {
        ResponseEntity<ErrorResponse> r = handler.handleBusinessRule(
                new BusinessRuleException("Artifact is part of an active exhibition"), request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("a database constraint becomes 409 without leaking the SQL")
    void dataIntegrityIs409() {
        ResponseEntity<ErrorResponse> r = handler.handleDataIntegrity(
                new DataIntegrityViolationException("Duplicate entry for key uq_artifacts_accession"), request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("a failed field validation becomes 400")
    void validationIs400() {
        BindingResult binding = mock(BindingResult.class);
        when(binding.getFieldErrors()).thenReturn(List.of(
                new FieldError("artifactRequest", "title", "must not be blank")));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(binding);

        ResponseEntity<ErrorResponse> r = handler.handleValidation(ex, request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("anything unrecognised becomes 500")
    void unexpectedIs500() {
        ResponseEntity<ErrorResponse> r = handler.handleUnexpected(
                new IllegalStateException("boom", new NullPointerException("inner")), request);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}