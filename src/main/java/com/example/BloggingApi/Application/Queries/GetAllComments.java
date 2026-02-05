package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GetAllComments {

    private final CommentRepository commentRepository;

    public GetAllComments(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Page<Comment> handle(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }
}

