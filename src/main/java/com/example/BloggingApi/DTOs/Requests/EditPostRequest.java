package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsPost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EditPostRequest(
        @NotNull
        @ExistsPost
        Long id,
        @NotBlank
        String title,
        @NotBlank
        String content
) {}
