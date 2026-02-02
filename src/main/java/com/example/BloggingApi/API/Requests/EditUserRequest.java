package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.UniqueEmail;
import com.example.BloggingApi.API.Validation.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EditUserRequest(
        @NotBlank
        Long id,
        @UniqueUsername
        String username,
        @Email
        @UniqueEmail
        String email
) {
}
