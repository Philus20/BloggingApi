package com.example.BloggingApi.API.Requests;

import com.example.BloggingApi.API.Validation.Numeric;
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
