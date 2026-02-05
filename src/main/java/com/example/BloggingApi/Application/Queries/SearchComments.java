package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchComments {

    private final CommentRepository commentRepository;

    public SearchComments(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Search comments by content
     */
    public Page<Comment> searchByContent(String content, Pageable pageable) {
        return commentRepository.findByContentContainingIgnoreCase(content, pageable);
    }

    /**
     * Search comments by author username
     */
    public Page<Comment> searchByAuthor(String username, Pageable pageable) {
        return commentRepository.findByAuthorUsernameContainingIgnoreCase(username, pageable);
    }
}
