package com.example.BloggingApi.DTOs.Responses;

public record UserResponse(
        Long id,
        String username,
        String email
) { }
