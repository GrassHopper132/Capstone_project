package org.peopleshores.capstone_project.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.dto.AuthResponse;
import org.peopleshores.capstone_project.dto.LoginRequest;
import org.peopleshores.capstone_project.dto.RegisterRequest;
import org.peopleshores.capstone_project.entity.Role;
import org.peopleshores.capstone_project.entity.User;
import org.peopleshores.capstone_project.exception.DuplicateResourceException;
import org.peopleshores.capstone_project.exception.InvalidCredentialsException;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.RoleRepository;
import org.peopleshores.capstone_project.repository.UserRepository;
import org.peopleshores.capstone_project.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The registration endpoint is unauthenticated, so the single most important
 * thing these tests pin down is that a caller cannot name their own role. An
 * earlier version of this service honoured the role field on the request, which
 * let anyone mint an administrator by asking for one.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService service;

    private RegisterRequest registration(String email, String requestedRole) {
        RegisterRequest r = mock(RegisterRequest.class);
        lenient().when(r.email()).thenReturn(email);
        lenient().when(r.password()).thenReturn("Password123!");
        lenient().when(r.fullName()).thenReturn("Jane Visitor");
        lenient().when(r.role()).thenReturn(requestedRole);
        return r;
    }

    private LoginRequest credentials(String email, String password) {
        LoginRequest r = mock(LoginRequest.class);
        lenient().when(r.email()).thenReturn(email);
        lenient().when(r.password()).thenReturn(password);
        return r;
    }

    private Role role(String name) {
        Role r = mock(Role.class);
        lenient().when(r.getName()).thenReturn(name);
        return r;
    }

    // --- registration -----------------------------------------------------

    @Test
    @DisplayName("refuses a second account on the same address")
    void registerRejectsExistingEmail() {
        when(userRepository.existsByEmail("taken@museum.org")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registration("taken@museum.org", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@museum.org");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("ignores a self-assigned ADMIN role and creates a VISITOR")
    void registerRefusesSelfAssignedPrivilege() {
        Role visitor = role("VISITOR");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("VISITOR")).thenReturn(Optional.of(visitor));
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashed");
        when(jwtService.generate(anyString(), anyString())).thenReturn("token");

        AuthResponse response = service.register(registration("attacker@example.com", "ADMIN"));

        assertThat(response.role()).isEqualTo("VISITOR");
        verify(roleRepository, never()).findByName("ADMIN");
    }

    @Test
    @DisplayName("stores the password as a hash, never as typed")
    void registerHashesThePassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        Role visitorRole = role("VISITOR");
        when(roleRepository.findByName("VISITOR")).thenReturn(Optional.of(visitorRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashed");
        when(jwtService.generate(anyString(), anyString())).thenReturn("token");

        service.register(registration("new@example.com", null));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("Password123!");
    }

    @Test
    @DisplayName("reports a missing VISITOR role rather than creating a user without one")
    void registerFailsWhenDefaultRoleMissing() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("VISITOR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(registration("new@example.com", null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    // --- login ------------------------------------------------------------

    @Test
    @DisplayName("rejects an address with no active account")
    void loginRejectsUnknownAccount() {
        when(userRepository.findByEmailAndActiveTrue("ghost@museum.org")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(credentials("ghost@museum.org", "Password123!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("rejects a wrong password without saying which half was wrong")
    void loginRejectsWrongPassword() {
        User user = new User("curator@museum.org", "$2a$10$hashed", "A Curator", role("CURATOR"));
        when(userRepository.findByEmailAndActiveTrue("curator@museum.org")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.login(credentials("curator@museum.org", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generate(anyString(), anyString());
    }

    @Test
    @DisplayName("returns a signed token and the account's own role")
    void loginIssuesTokenForValidCredentials() {
        User user = new User("curator@museum.org", "$2a$10$hashed", "A Curator", role("CURATOR"));
        when(userRepository.findByEmailAndActiveTrue("curator@museum.org")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "$2a$10$hashed")).thenReturn(true);
        when(jwtService.generate("curator@museum.org", "CURATOR")).thenReturn("signed.jwt.value");
        when(jwtService.getExpirySeconds()).thenReturn(1800L);

        AuthResponse response = service.login(credentials("curator@museum.org", "Password123!"));

        assertThat(response.token()).isEqualTo("signed.jwt.value");
        assertThat(response.email()).isEqualTo("curator@museum.org");
        assertThat(response.fullName()).isEqualTo("A Curator");
        assertThat(response.role()).isEqualTo("CURATOR");
    }
}