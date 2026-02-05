package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.CommentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteComment {

    private final CommentRepository commentRepository;

    public DeleteComment(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void handle(Long commentId) throws NullException {
        Comment comment = commentRepository.findByInteger(commentId.intValue());
        
        if (comment == null) {
            throw new NullException("Comment not found");
        }

        commentRepository.delete(commentId.intValue());
    }
}
