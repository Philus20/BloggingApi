package com.example.BloggingApi.ResposesDTO;

import com.example.BloggingApi.Entities.Tag;

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
