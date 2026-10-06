package org.peopleshores.capstone_project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/** Issues and validates the bearer tokens used by every secured endpoint. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expiryMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiry-minutes:30}") long expiryMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMillis = expiryMinutes * 60_000L;
    }

    public String generate(String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("role", role))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMillis))
                .signWith(key)
                .compact();
    }

    /** Returns the subject, or null when the token is missing, expired or forged. */
    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public long getExpirySeconds() {
        return expiryMillis / 1000;
    }
}