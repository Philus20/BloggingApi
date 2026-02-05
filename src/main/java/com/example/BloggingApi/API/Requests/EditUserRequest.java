package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.UniqueEmail;
import com.example.BloggingApi.API.Validation.UniqueUsername;
import jakarta.validation.constraints.Email;

public record EditUserRequest(
        Long id,
        @UniqueUsername
        String username,
        @Email

        String email
) {
}
