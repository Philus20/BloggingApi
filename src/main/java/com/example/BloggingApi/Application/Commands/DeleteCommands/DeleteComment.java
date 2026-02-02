package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteComment {

    private final CommentRepository commentRepository;

    public DeleteComment(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void handle(Long commentId) throws NullException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NullException("Comment not found"));

        commentRepository.delete(comment);
    }
}
