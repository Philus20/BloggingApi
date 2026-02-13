package com.example.BloggingApi.RequestsDTO;

import com.example.BloggingApi.Validation.Numeric;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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
