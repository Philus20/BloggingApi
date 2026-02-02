package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class GetCommentById {

    private final CommentRepository commentRepository;

    public GetCommentById(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment handle(Long commentId) throws NullException {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NullException("Comment not found"));
    }
}
