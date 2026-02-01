package com.example.BloggingApi.API.Requests;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank
    String title,
    @NotBlank
    String content,
    @NotBlank
    Long authorId
){

}
