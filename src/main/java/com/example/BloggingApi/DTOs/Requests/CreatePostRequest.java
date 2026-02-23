package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
        @NotBlank
        String title,
        @NotBlank
        String content,
        @NotNull
        @ExistsUser
        Long authorId
) {
}
