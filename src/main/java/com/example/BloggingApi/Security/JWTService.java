package com.example.BloggingApi.Security;

import com.example.BloggingApi.Domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class JWTService {

    private static final Logger log = LoggerFactory.getLogger(JWTService.class);
    private static final int HMAC_SHA256_MIN_BYTES = 32;

    @Value("${app.jwt.secret:defaultsecret}")
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

        log.debug("Generating token - Now: {}, Expiry: {}", now, expiry);
        
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
        return extractClaim(token, Claims::getId);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return true;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaimsWithValidation(token);
        return resolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims extractAllClaimsWithValidation(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        String key = secret;
        if (key != null && key.startsWith(" ")) {
            key = key.trim();
            log.debug("JWT secret had leading whitespace - trimmed");
        }
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < HMAC_SHA256_MIN_BYTES) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HMAC SHA-256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
