package com.example.BloggingApi.Security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Keeps track of which tokens are currently in use
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

    public List<SessionInfo> getAll() {
        return new ArrayList<>(active.values());
    }

    public int size() {
        return active.size();
    }
}
