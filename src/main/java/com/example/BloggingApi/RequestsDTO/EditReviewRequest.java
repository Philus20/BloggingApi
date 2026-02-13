package com.example.BloggingApi.RequestsDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record EditReviewRequest(
        Long id,
        @Min(1)
        @Max(5)
        int rating,
        String comment
) {
}
