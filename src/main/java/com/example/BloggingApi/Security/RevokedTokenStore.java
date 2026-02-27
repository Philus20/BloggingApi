package com.example.BloggingApi.Security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Blacklisted JWT IDs — cleaned up on a schedule once they naturally expire
@Component
public class RevokedTokenStore {

    private final Map<String, Long> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, long expiresAtMs) {
        if (jti != null && !jti.isBlank()) {
            revoked.put(jti, expiresAtMs);
        }
    }

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
