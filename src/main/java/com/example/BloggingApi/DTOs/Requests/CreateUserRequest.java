package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.UniqueEmail;
import com.example.BloggingApi.DTOs.Validation.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank
        @UniqueUsername
        String username,

        @Email
        @UniqueEmail
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character"
        )
        String password
) { }
