package org.peopleshores.capstone_project.exception;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(nullable = false)
    private String role; // 'ADMIN', 'CURATOR', 'RESTORER'

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    // Getters and Setters omitted for brevity
}
