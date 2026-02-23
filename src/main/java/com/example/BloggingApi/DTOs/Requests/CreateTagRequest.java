package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.UniqueTagName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank
        @Size(max = 50)
        @UniqueTagName
        String name
) {
}
