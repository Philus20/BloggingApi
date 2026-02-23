package com.example.BloggingApi.DTOs.Responses;

import com.example.BloggingApi.Domain.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String authorUsername,
        Long postId,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getPost().getId(),
                comment.getCreatedAt()
        );
    }
}
