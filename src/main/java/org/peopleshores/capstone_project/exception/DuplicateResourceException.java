package org.peopleshores.capstone_project.exception;

/** Thrown when a uniqueness rule is violated. Mapped to 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}