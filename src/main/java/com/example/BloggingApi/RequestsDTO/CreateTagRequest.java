package com.example.BloggingApi.RequestsDTO;

import com.example.BloggingApi.Validation.UniqueTagName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank
        @Size(max = 50)
        @UniqueTagName
        String name
) {
}
