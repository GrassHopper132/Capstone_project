package org.peopleshores.capstone_project.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peopleshores.capstone_project.entity.Role;
import org.peopleshores.capstone_project.entity.User;
import org.peopleshores.capstone_project.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The ROLE_ prefix added here is what makes hasRole('ADMIN') work in the
 * controllers. Drop it and every @PreAuthorize silently denies everyone, so it
 * is asserted explicitly rather than assumed.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppUserDetailsService")
class AppUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private AppUserDetailsService service;

    private User account(String email, String roleName) {
        Role role = mock(Role.class);
        lenient().when(role.getName()).thenReturn(roleName);
        return new User(email, "$2a$10$hashed", "A Person", role);
    }

    @Test
    @DisplayName("grants the account's role with the ROLE_ prefix Spring expects")
    void addsRolePrefix() {
        User admin = account("admin@museum.org", "ADMIN");
        when(userRepository.findByEmailAndActiveTrue("admin@museum.org"))
                .thenReturn(Optional.of(admin));

        UserDetails details = service.loadUserByUsername("admin@museum.org");

        assertThat(details.getUsername()).isEqualTo("admin@museum.org");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("carries a visitor's role through unchanged apart from the prefix")
    void handlesEveryRole() {
        User visitor = account("visitor@example.com", "VISITOR");
        when(userRepository.findByEmailAndActiveTrue("visitor@example.com"))
                .thenReturn(Optional.of(visitor));

        assertThat(service.loadUserByUsername("visitor@example.com").getAuthorities())
                .extracting("authority").containsExactly("ROLE_VISITOR");
    }

    @Test
    @DisplayName("refuses an address with no active account")
    void refusesInactiveOrUnknownAccount() {
        when(userRepository.findByEmailAndActiveTrue("gone@museum.org")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("gone@museum.org"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("gone@museum.org");
    }
}