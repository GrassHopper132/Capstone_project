package org.peopleshores.capstone_project.exception;

import java.time.Instant;
import java.util.Map;

/** One JSON error shape for the whole API, as documented in docs/API.md. */
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private Map<String, String> validationErrors;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}