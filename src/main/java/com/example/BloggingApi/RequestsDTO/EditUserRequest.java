package com.example.BloggingApi.RequestsDTO;

import com.example.BloggingApi.Validation.UniqueUsername;
import jakarta.validation.constraints.Email;

public record EditUserRequest(
        Long id,
        @UniqueUsername
        String username,
        @Email

        String email
) {
}
