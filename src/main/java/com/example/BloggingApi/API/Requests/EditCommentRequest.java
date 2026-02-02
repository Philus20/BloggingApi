package com.example.BloggingApi.API.Requests;

import jakarta.validation.constraints.NotBlank;

public record EditCommentRequest(
        @NotBlank
        Long id,
        @NotBlank
        String content
) {
}
