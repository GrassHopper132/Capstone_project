package org.peopleshores.capstone_project.dto;

/** The token and just enough about the user for the UI to render. */
public record AuthResponse(
        String token,
        long expiresIn,
        String email,
        String fullName,
        String role
) {
}