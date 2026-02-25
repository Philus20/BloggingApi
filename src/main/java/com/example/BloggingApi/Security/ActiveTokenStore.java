package com.example.BloggingApi.Security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory map of active token sessions (jti -> session info) for tracking and reporting.
 * Updated on login (add) and logout (remove).
 */
@Component
public class ActiveTokenStore {

    public record SessionInfo(String jti, String username, long issuedAtMs) {}

    private final Map<String, SessionInfo> active = new ConcurrentHashMap<>();

    public void add(String jti, String username, long issuedAtMs) {
        if (jti != null && !jti.isBlank() && username != null) {
            active.put(jti, new SessionInfo(jti, username, issuedAtMs));
        }
    }

    public void remove(String jti) {
        if (jti != null) {
            active.remove(jti);
        }
    }

    /** Returns a snapshot of currently tracked active sessions (for reporting). */
    public List<SessionInfo> getAll() {
        return new ArrayList<>(active.values());
    }

    public int size() {
        return active.size();
    }
}
