package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EditCommentRequest(
        @NotNull
        @ExistsComment
        Long id,
        @NotBlank
        String content
) {
}
