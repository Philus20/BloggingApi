package com.example.BloggingApi.DTOs.Requests;

public record LoginRequest (
        String username,
        String password
) { }
