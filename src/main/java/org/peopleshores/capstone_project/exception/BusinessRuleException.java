package org.peopleshores.capstone_project.exception;

/**
 * Thrown when a request is well-formed but breaks a domain rule, such as
 * deleting an artifact that is on an active exhibition (FR-11). Mapped to 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}