package com.example.BloggingApi.DTOs.Responses;

import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentResponseTest {

    @Test
    void from_shouldMapCommentToResponse() {
        User author = User.create("author", "a@e.com", "p");
        Post post = Post.create("Post", "Content", author);
        Comment comment = Comment.create("Comment body", post, author);
        CommentResponse res = CommentResponse.from(comment);
        assertNotNull(res);
        assertEquals("Comment body", res.content());
        assertEquals("author", res.authorUsername());
        assertNotNull(res.createdAt());
    }
}
