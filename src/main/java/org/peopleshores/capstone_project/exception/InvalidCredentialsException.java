package org.peopleshores.capstone_project.exception;

/** Wrong email or password. Mapped to 401, deliberately without saying which was wrong. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email or password is incorrect.");
    }
}