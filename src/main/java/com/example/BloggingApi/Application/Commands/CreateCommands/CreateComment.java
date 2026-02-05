package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateCommentRequest;
import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.CommentRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.PostRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateComment {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CreateComment(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Comment handle(CreateCommentRequest req)  {
        // Validation
        if (req.content() == null || req.content().isBlank()) {
            throw new NullException("Content cannot be blank");
        }
        if (req.postId() == null) {
            throw new NullException("Post ID cannot be null");
        }
        if (req.authorId() == null) {
            throw new NullException("Author ID cannot be null");
        }

        // Fetch Post entity
        Post post = postRepository.findByInteger(req.postId().intValue());
        
        if (post == null) {
            throw new NullException("Post not found");
        }

        // Fetch User entity
        User author = userRepository.findByInteger(req.authorId().intValue());
        
        if (author == null) {
            throw new NullException("Author not found");
        }

        // Create Comment entity
        Comment comment = Comment.create(req.content(), post, author);

        // Save to DB
        commentRepository.create(comment);

        return comment;
    }
}
