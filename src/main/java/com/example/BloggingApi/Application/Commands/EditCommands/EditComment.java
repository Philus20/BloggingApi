package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditCommentRequest;
import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.CommentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EditComment {

    private final CommentRepository commentRepository;

    public EditComment(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment handle(EditCommentRequest request) throws NullException {
        Comment comment = commentRepository.findByInteger(request.id().intValue());
        
        if (comment == null) {
            throw new NullException("Comment not found");
        }

        comment.update(request.content());

        return comment;
    }
}
