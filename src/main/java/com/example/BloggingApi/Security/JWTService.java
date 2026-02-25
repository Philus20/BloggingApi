package com.example.BloggingApi.Security;

import com.example.BloggingApi.Domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Generates and validates JWTs signed with HMAC SHA-256.
 * Standard claims: sub (username), iat (issued at), exp (expiration).
 * Custom claims: role (user role), email.
 */
@Service
public class JWTService {

    private static final int HMAC_SHA256_MIN_BYTES = 32;

    @Value("${app.jwt.secret:BloggingApiJwtSecretKeyForSigningMustBeAtLeast32BytesLong}")
    private String secret;

    @Value("${app.jwt.expiration-ms:3600000}")
    private long expirationMs;

    /** Returns token validity duration in seconds (for inclusion in login response). */
    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    /**
     * Generates a signed JWT with subject (username), jti (ID), issued-at, expiration, and custom claims (role, email).
     * Algorithm: HMAC SHA-256. Hashing is used for password storage (BCrypt) and token verification (signature).
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole() != null ? user.getRole().trim().toUpperCase() : "READER");
        claims.put("email", user.getEmail());
        String jti = UUID.randomUUID().toString();

        Date now = new Date();
        Date expiry = new Date(System.currentTimeMillis() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .id(jti)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /** Extracts the JWT ID (jti) claim for blacklisting and session tracking. */
    public String extractJti(String token) {
        Object jti = extractAllClaims(token).get("jti");
        return jti instanceof String s ? s : null;
    }

    /**
     * Validates the token: signature, subject match, and not expired.
     * @throws JwtException (e.g. ExpiredJwtException, SignatureException) if invalid or expired
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Returns true if the token is expired (for use after parsing). */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public List<String> extractRoles(String token) {
        Object role = extractAllClaims(token).get("role");
        if (role instanceof String s) {
            return List.of(s);
        }
        return List.of();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Parses and verifies the token (signature and expiry). Use for validation only.
     * @throws ExpiredJwtException if the token is expired
     * @throws SignatureException if the signature is invalid (tampered)
     * @throws JwtException for other parse/validation failures
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < HMAC_SHA256_MIN_BYTES) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HMAC SHA-256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
