package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.Security.ActiveTokenStore;
import com.example.BloggingApi.Security.SecurityEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only endpoints for security event reports and token/session tracking (Epic 5, User Story 5.2).
 */
@RestController
@RequestMapping("/api/v1/admin/security")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Security reports", description = "Authentication and token usage reports (ADMIN only)")
public class SecurityReportController {

    private final SecurityEventService securityEventService;
    private final ActiveTokenStore activeTokenStore;

    public SecurityReportController(SecurityEventService securityEventService, ActiveTokenStore activeTokenStore) {
        this.securityEventService = securityEventService;
        this.activeTokenStore = activeTokenStore;
    }

    @GetMapping("/events")
    @Operation(summary = "Recent security events", description = "Login success/failure, token rejected/revoked, and request events for audit and brute-force analysis.")
    public ApiResponse<List<SecurityEventService.SecurityEvent>> getRecentEvents(
            @RequestParam(defaultValue = "100") int max) {
        List<SecurityEventService.SecurityEvent> events = securityEventService.getRecentEvents(Math.min(max, 500));
        return ApiResponse.success("Security events (use logs for full history)", events);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Active token sessions", description = "Currently tracked active sessions (token usage / access frequency).")
    public ApiResponse<Map<String, Object>> getActiveSessions() {
        List<ActiveTokenStore.SessionInfo> sessions = activeTokenStore.getAll();
        return ApiResponse.success("Active sessions", Map.of(
                "count", activeTokenStore.size(),
                "sessions", sessions
        ));
    }
}
