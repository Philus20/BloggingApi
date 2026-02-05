package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.Numeric;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateReviewRequest(
        @Min(1)
        @Max(5)
        int rating,
        String comment,
        @Numeric

        Long userId,
        @Numeric

        Long postId
) {
}
