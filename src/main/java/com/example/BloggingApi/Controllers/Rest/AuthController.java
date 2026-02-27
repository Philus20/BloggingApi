package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.LoginResponse;
import com.example.BloggingApi.Security.ActiveTokenStore;
import com.example.BloggingApi.Security.JWTService;
import com.example.BloggingApi.Security.RevokedTokenStore;
import com.example.BloggingApi.Security.SecurityEventService;
import com.example.BloggingApi.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "JWT authentication")
public class AuthController {

    private final UserService userService;
    private final JWTService jwtService;
    private final RevokedTokenStore revokedTokenStore;
    private final ActiveTokenStore activeTokenStore;
    private final SecurityEventService securityEventService;

    public AuthController(UserService userService, JWTService jwtService,
                          RevokedTokenStore revokedTokenStore, ActiveTokenStore activeTokenStore,
                          SecurityEventService securityEventService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.revokedTokenStore = revokedTokenStore;
        this.activeTokenStore = activeTokenStore;
        this.securityEventService = securityEventService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password. Returns a signed JWT.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, JWT returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bad credentials (wrong username or password)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many failed attempts — temporarily locked")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @jakarta.validation.Valid com.example.BloggingApi.DTOs.Requests.LoginRequest request) {
        if (securityEventService.isBlocked(request.username())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.failure("Account temporarily locked due to too many failed attempts. Try again in a few minutes."));
        }

        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            securityEventService.logLoginFailure(request.username(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    @Operation(summary = "Logout", description = "Revoke the current Bearer token. Token is blacklisted.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token")
    })
    public ApiResponse<Void> logout(HttpServletRequest request, Authentication authentication) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String jti = jwtService.extractJti(token);
                long expMs = jwtService.extractExpiration(token).getTime();
                String username = jwtService.extractUsername(token);
                if (jti != null) {
                    revokedTokenStore.revoke(jti, expMs);
                    activeTokenStore.remove(jti);
                    securityEventService.logTokenRevoked(username, jti);
                }
            } catch (Exception ignored) {}
        }
        return ApiResponse.success("Logged out successfully");
    }
}
