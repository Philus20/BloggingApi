package com.example.BloggingApi.API.Requests;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank
        String content,
        @NotBlank
        Long postId,
        @NotBlank
        Long authorId
) {
}
