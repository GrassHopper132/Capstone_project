package org.peopleshores.capstone_project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The filter must let every request through whatever happens: refusing a bad
 * token is the filter chain's job, not this filter's. If it stopped the chain
 * on a bad token the caller would get a blank response instead of the JSON 401
 * the API promises.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private AppUserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private org.springframework.security.core.userdetails.User principal(String email, String role) {
        return new org.springframework.security.core.userdetails.User(
                email, "$2a$10$hashed", List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    @DisplayName("lets an unauthenticated request through without inventing a user")
    void passesThroughWithoutHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verify(jwtService, never()).extractEmail(anyString());
    }

    @Test
    @DisplayName("ignores an Authorization header that is not a bearer token")
    void ignoresNonBearerHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verify(jwtService, never()).extractEmail(anyString());
    }

    @Test
    @DisplayName("signs in the account named by a valid token")
    void authenticatesValidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer good.jwt.value");
        when(jwtService.extractEmail("good.jwt.value")).thenReturn("curator@museum.org");
        when(userDetailsService.loadUserByUsername("curator@museum.org"))
                .thenReturn(principal("curator@museum.org", "CURATOR"));

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_CURATOR");
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("stays anonymous when the token cannot be read")
    void staysAnonymousOnUnreadableToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer forged.or.expired");
        when(jwtService.extractEmail("forged.or.expired")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("stays anonymous when the token names an account that has been deactivated")
    void staysAnonymousWhenAccountIsGone() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.but.orphaned");
        when(jwtService.extractEmail("valid.but.orphaned")).thenReturn("deleted@museum.org");
        when(userDetailsService.loadUserByUsername("deleted@museum.org"))
                .thenThrow(new UsernameNotFoundException("No active account"));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}