package com.example.BloggingApi.DTOs.Responses;

import com.example.BloggingApi.Domain.Tag;

public record TagResponse(
        Long id,
        String name
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName()
        );
    }
}
