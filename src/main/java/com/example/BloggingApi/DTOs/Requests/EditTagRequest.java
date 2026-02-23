package com.example.BloggingApi.DTOs.Requests;

import com.example.BloggingApi.DTOs.Validation.ExistsTag;
import com.example.BloggingApi.DTOs.Validation.UniqueTagNameForEdit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@UniqueTagNameForEdit
public record EditTagRequest(
        @NotNull
        @ExistsTag
        Long id,
        @NotBlank
        @Size(max = 50)
        String name
) {
}
