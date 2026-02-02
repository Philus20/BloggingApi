package com.example.BloggingApi.API.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EditUserRequest(
        @NotBlank
        Long id,
        String username,
        @Email
        String email
) {
}
