package org.peopleshores.capstone_project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be at least 8 characters") String password,
        @NotBlank @Size(max = 120) String fullName,
        String role
) {
}