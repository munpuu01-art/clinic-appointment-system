package com.clinic.security;

import com.clinic.config.ClinicProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/** ออกและตรวจสอบ JWT — แยกความรับผิดชอบเรื่อง token ไว้ที่คลาสเดียว (SRP) */
@Service
public class JwtTokenService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtTokenService(ClinicProperties properties) {
        this.key = Keys.hmacShaKeyFor(
                properties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = properties.getSecurity().getJwtExpirationMinutes();
    }

    public String issue(AppUserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationMinutes * 60);
        return Jwts.builder()
                .subject(principal.getUsername())
                .claims(Map.of(
                        "accountId", principal.getAccountId(),
                        "role", principal.getRole().name(),
                        "personId", principal.getPersonId() == null ? -1L : principal.getPersonId(),
                        "name", principal.getDisplayName()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long expiresInSeconds() { return expirationMinutes * 60; }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
