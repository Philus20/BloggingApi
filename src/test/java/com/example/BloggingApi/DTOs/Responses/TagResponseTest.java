package com.example.BloggingApi.DTOs.Responses;

import com.example.BloggingApi.Domain.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagResponseTest {

    @Test
    void from_shouldMapTagToResponse() {
        Tag tag = Tag.create("java");
        TagResponse res = TagResponse.from(tag);
        assertNotNull(res);
        assertEquals("java", res.name());
    }
}
