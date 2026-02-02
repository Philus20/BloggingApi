package com.example.BloggingApi.API.Resposes;

import com.example.BloggingApi.Domain.Entities.Post;

import java.time.LocalDateTime;

public record PostsResponse(
        Long id,
        String title,
        String content,
        String authorUsername,
        LocalDateTime createdAt
) {
    public static PostsResponse from(Post post) {
        return new PostsResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getCreatedAt()
        );
    }
}
