package com.example.BloggingApi.RequestsDTO;

import com.example.BloggingApi.Validation.Numeric;
import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank
    String title,
    @NotBlank
    String content,
        @Numeric

    Long authorId
){

}
