package com.example.BloggingApi.API.Resposes;

public record UserResponse(
        Long id,
        String username,
        String email
) { }
