package com.example.BloggingApi.Security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for revoked JWT IDs (blacklist). Used for logout and token invalidation.
 * Entries expire after the token's natural expiry time; expired entries are cleaned periodically.
 */
@Component
public class RevokedTokenStore {

    /** jti -> expiresAt (epoch ms). Token is revoked if present and not yet expired. */
    private final Map<String, Long> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, long expiresAtMs) {
        if (jti != null && !jti.isBlank()) {
            revoked.put(jti, expiresAtMs);
        }
    }

    /** Returns true if the token ID has been revoked and not yet expired. */
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Long expiresAt = revoked.get(jti);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt < System.currentTimeMillis()) {
            revoked.remove(jti);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRateString = "${app.security.revoked-token-cleanup-ms:300000}")
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        revoked.entrySet().removeIf(e -> e.getValue() < now);
    }
}
