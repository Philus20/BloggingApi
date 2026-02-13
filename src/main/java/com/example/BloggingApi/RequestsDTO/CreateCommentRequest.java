package com.example.BloggingApi.RequestsDTO;

import com.example.BloggingApi.Validation.Numeric;
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
