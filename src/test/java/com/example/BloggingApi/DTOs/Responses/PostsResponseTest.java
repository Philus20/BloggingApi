package com.example.BloggingApi.DTOs.Responses;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PostsResponseTest {

    @Test
    void from_shouldMapPostToResponse() {
        User author = User.create("author", "a@e.com", "p");
        Post post = Post.create("My Title", "My content", author);
        PostsResponse res = PostsResponse.from(post);
        assertNotNull(res);
        assertEquals("My Title", res.title());
        assertEquals("My content", res.content());
        assertEquals("author", res.authorUsername());
        assertNotNull(res.createdAt());
    }
}
