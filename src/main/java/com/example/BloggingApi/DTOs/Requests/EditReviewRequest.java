package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsReview;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EditReviewRequest(
        @NotNull
        @ExistsReview
        Long id,
        @Min(1)
        @Max(5)
        int rating,
        String comment
) {
}
