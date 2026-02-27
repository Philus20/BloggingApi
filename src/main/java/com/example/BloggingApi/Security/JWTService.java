package com.example.BloggingApi.Security;

import com.example.BloggingApi.Domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

// JWT signing and validation (HMAC SHA-256)
@Service
public class JWTService {

    private static final int HMAC_SHA256_MIN_BYTES = 32;

    @Value("${app.jwt.secret:BloggingApiJwtSecretKeyForSigningMustBeAtLeast32BytesLong}")
    private String secret;

    @Value("${app.jwt.expiration-ms:3600000}")
    private long expirationMs;

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

    public String extractJti(String token) {
        Object jti = extractAllClaims(token).get("jti");
        return jti instanceof String s ? s : null;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

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
