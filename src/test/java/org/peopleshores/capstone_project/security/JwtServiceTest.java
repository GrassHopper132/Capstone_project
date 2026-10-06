package org.peopleshores.capstone_project.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * extractEmail returns null rather than throwing for every bad token, so these
 * tests exist to prove that a forged or expired token is rejected quietly and
 * is never mistaken for an anonymous request that happens to be allowed.
 */
@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET =
            "test-only-signing-key-not-used-anywhere-outside-the-test-suite-0123456789";
    private static final String OTHER_SECRET =
            "a-completely-different-key-belonging-to-nobody-9876543210-abcdefghij";

    private final JwtService service = new JwtService(SECRET, 30);

    @Test
    @DisplayName("a token it issued yields back the address it was issued for")
    void tokenRoundTripsTheSubject() {
        String token = service.generate("curator@museum.org", "CURATOR");

        assertThat(service.extractEmail(token)).isEqualTo("curator@museum.org");
    }

    @Test
    @DisplayName("tokens for two accounts are not interchangeable")
    void tokensAreAccountSpecific() {
        String one = service.generate("curator@museum.org", "CURATOR");
        String two = service.generate("admin@museum.org", "ADMIN");

        assertThat(one).isNotEqualTo(two);
        assertThat(service.extractEmail(two)).isEqualTo("admin@museum.org");
    }

    @Test
    @DisplayName("refuses a token signed with somebody else's key")
    void rejectsForgedToken() {
        JwtService attacker = new JwtService(OTHER_SECRET, 30);
        String forged = attacker.generate("admin@museum.org", "ADMIN");

        assertThat(service.extractEmail(forged)).isNull();
    }

    @Test
    @DisplayName("refuses a token whose lifetime has run out")
    void rejectsExpiredToken() {
        JwtService alreadyExpired = new JwtService(SECRET, -5);
        String stale = alreadyExpired.generate("curator@museum.org", "CURATOR");

        assertThat(service.extractEmail(stale)).isNull();
    }

    @Test
    @DisplayName("refuses text that is not a token at all")
    void rejectsGarbage() {
        assertThat(service.extractEmail("not-a-token")).isNull();
        assertThat(service.extractEmail("")).isNull();
        assertThat(service.extractEmail(null)).isNull();
    }

    @Test
    @DisplayName("reports its lifetime in seconds for the client to count down")
    void reportsExpiryInSeconds() {
        assertThat(service.getExpirySeconds()).isEqualTo(1800L);
        assertThat(new JwtService(SECRET, 15).getExpirySeconds()).isEqualTo(900L);
    }
}