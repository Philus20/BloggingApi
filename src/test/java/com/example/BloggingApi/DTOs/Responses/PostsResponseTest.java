package com.example.BloggingApi.DTOs.Responses;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostsResponseTest {

    @Test
    void from_shouldMapPostToResponse() {
        User author = User.create("author1", "author@test.com", "pass");
        Post post = Post.create("My Title", "My content", author);

        PostsResponse response = PostsResponse.from(post);

        assertNotNull(response);
        assertEquals("My Title", response.title());
        assertEquals("My content", response.content());
        assertEquals("author1", response.authorUsername());
    }
}
