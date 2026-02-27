package com.example.BloggingApi.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Tracks login attempts and security events in memory
@Service
public class SecurityEventService {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventService.class);

    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        TOKEN_REVOKED,
        TOKEN_REJECTED
    }

    public record SecurityEvent(EventType type, String username, String details, long timestampMs) {
        public String getTimestampIso() {
            return Instant.ofEpochMilli(timestampMs).toString();
        }
    }

    private static final int MAX_EVENTS = 1000;
    private final List<SecurityEvent> recentEvents = new ArrayList<>();
    private final Object eventsLock = new Object();

    private final Map<String, AtomicLong> failureCountByUser = new ConcurrentHashMap<>();
    private static final int BRUTE_FORCE_THRESHOLD = 5;
    private static final long BRUTE_FORCE_WINDOW_MS = 5 * 60 * 1000; // 5 minutes
    private final Map<String, Long> windowStartByUser = new ConcurrentHashMap<>();

    public void logLoginSuccess(String username) {
        if (username != null) {
            recordSuccess(username);
            addEvent(EventType.LOGIN_SUCCESS, username, "Login successful");
            log.info("SECURITY_AUTH success username={}", username);
        }
    }

    public void logLoginFailure(String username, String reason) {
        if (username != null) {
            recordFailure(username);
            addEvent(EventType.LOGIN_FAILURE, username, reason != null ? reason : "Invalid credentials");
            log.warn("SECURITY_AUTH failure username={} reason={}", username, reason);
        }
    }

    public void logTokenRejected(String username, String reason) {
        addEvent(EventType.TOKEN_REJECTED, username, reason);
        log.warn("SECURITY_TOKEN rejected username={} reason={}", username, reason);
    }

    public void logTokenRevoked(String username, String jti) {
        addEvent(EventType.TOKEN_REVOKED, username, "jti=" + jti);
        log.info("SECURITY_TOKEN revoked username={} jti={}", username, jti);
    }

    // Too many failed attempts within the window?
    public boolean isBlocked(String username) {
        if (username == null || username.isBlank()) return false;
        long now = System.currentTimeMillis();
        Long start = windowStartByUser.get(username);
        if (start == null || now - start > BRUTE_FORCE_WINDOW_MS) {
            return false;
        }
        AtomicLong count = failureCountByUser.get(username);
        return count != null && count.get() >= BRUTE_FORCE_THRESHOLD;
    }

    public void recordFailure(String username) {
        if (username == null) return;
        long now = System.currentTimeMillis();
        windowStartByUser.compute(username, (k, start) -> start == null || now - start > BRUTE_FORCE_WINDOW_MS ? now : start);
        failureCountByUser.computeIfAbsent(username, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void recordSuccess(String username) {
        if (username != null) {
            failureCountByUser.remove(username);
            windowStartByUser.remove(username);
        }
    }

    public List<SecurityEvent> getRecentEvents(int max) {
        synchronized (eventsLock) {
            int size = recentEvents.size();
            if (size <= max) return new ArrayList<>(recentEvents);
            return new ArrayList<>(recentEvents.subList(size - max, size));
        }
    }

    private void addEvent(EventType type, String username, String details) {
        SecurityEvent event = new SecurityEvent(type, username, details, System.currentTimeMillis());
        synchronized (eventsLock) {
            recentEvents.add(event);
            if (recentEvents.size() > MAX_EVENTS) {
                recentEvents.remove(0);
            }
        }
    }
}
