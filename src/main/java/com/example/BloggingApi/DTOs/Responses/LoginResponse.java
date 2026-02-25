package com.example.BloggingApi.DTOs.Responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Login response containing JWT and user identity for protected endpoints")
public record LoginResponse(
        @Schema(description = "JWT token; send in Authorization: Bearer <token> for protected requests") String token,
        @Schema(description = "Authenticated username (JWT subject)") String username,
        @Schema(description = "User roles (e.g. READER, AUTHOR, ADMIN)") List<String> roles,
        @Schema(description = "Token expiration timestamp (Unix seconds) for verification") long expiresAt
) {}
