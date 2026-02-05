package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.Numeric;
import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank
        String content,

        @Numeric
        Long postId,
        @Numeric

        Long authorId
) {
}
