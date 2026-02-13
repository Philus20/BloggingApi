package com.example.BloggingApi.ResposesDTO;

import com.example.BloggingApi.Entities.Comment;

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
