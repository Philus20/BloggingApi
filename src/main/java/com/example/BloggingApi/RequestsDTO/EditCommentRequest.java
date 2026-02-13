package com.example.BloggingApi.RequestsDTO;

import com.example.BloggingApi.Validation.Numeric;
import jakarta.validation.constraints.NotBlank;

public record EditCommentRequest(

        @Numeric
        Long id,
        @NotBlank
        String content
) {
}
