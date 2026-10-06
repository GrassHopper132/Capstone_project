package org.peopleshores.capstone_project.exception;

/** Thrown when an id does not resolve. Mapped to 404 by GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String type, Object id) {
        return new ResourceNotFoundException(type + " not found with id " + id);
    }
}