package com.example.BloggingApi.API.Requests;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank
        String username,

        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character"
        )
        String password
) { }
