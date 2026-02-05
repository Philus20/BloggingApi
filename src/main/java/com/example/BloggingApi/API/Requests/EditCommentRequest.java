package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.Numeric;
import jakarta.validation.constraints.NotBlank;

public record EditCommentRequest(

        @Numeric
        Long id,
        @NotBlank
        String content
) {
}
