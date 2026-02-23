package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsPost;
import com.example.BloggingApi.DTOs.Validation.ExistsUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotBlank
        String content,
        @NotNull
        @ExistsPost
        Long postId,
        @NotNull
        @ExistsUser
        Long authorId
) {
}
