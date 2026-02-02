package com.example.BloggingApi.API.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditTagRequest(
        @NotBlank
        Long id,
        @NotBlank
        @Size(max = 50)
        String name
) {
}
