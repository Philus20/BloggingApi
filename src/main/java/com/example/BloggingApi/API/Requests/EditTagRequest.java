package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.UniqueTagName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditTagRequest(
        @NotBlank
        Long id,
        @NotBlank
        @Size(max = 50)
        @UniqueTagName
        String name
) {
}
