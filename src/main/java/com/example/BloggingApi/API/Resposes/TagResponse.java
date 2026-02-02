package com.example.BloggingApi.API.Resposes;

import com.example.BloggingApi.Domain.Entities.Tag;

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
