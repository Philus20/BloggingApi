package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsPost;
import com.example.BloggingApi.DTOs.Validation.ExistsUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(
        @Min(1)
        @Max(5)
        int rating,
        String comment,
        @NotNull
        @ExistsUser
        Long userId,
        @NotNull
        @ExistsPost
        Long postId
) {
}
