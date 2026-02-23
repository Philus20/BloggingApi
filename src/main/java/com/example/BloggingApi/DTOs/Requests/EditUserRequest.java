package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsUser;
import com.example.BloggingApi.DTOs.Validation.UniqueUserForEdit;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@UniqueUserForEdit
public record EditUserRequest(
        @NotNull
        @ExistsUser
        Long id,
        String username,
        @Email
        String email
) {
}
