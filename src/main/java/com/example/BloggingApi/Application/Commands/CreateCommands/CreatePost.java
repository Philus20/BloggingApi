package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.PostRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreatePost {

  PostRepository postRepository;
    UserRepository userRepository;

    public CreatePost(PostRepository postRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public Post handle(CreatePostRequest req)  {
        //  Validation
        System.out.println(req.authorId());
        System.out.println(req.title());
        System.out.println(req.content());

        if (req.title() == null || req.title().isBlank()) {
            throw new NullException("Title cannot be blank");
        }
        if (req.content() == null || req.content().isBlank()) {
            throw new NullException("Content cannot be blank");
        }
        if (req.authorId() == null) {
            throw new NullException("Author Id cannot be blank");
        }

        // Fetch User entity
        User author = userRepository.findByInteger(req.authorId().intValue());
        
        if (author == null) {
            throw new NullException("Author not found");
        }

        // Create Post entity
        Post post = Post.create(req.title(), req.content(), author);

        // Save to DB
        postRepository.create(post);

        return post;
    }


}
