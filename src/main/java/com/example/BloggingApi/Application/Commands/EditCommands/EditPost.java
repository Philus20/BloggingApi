package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditPostRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class EditPost {

    private final PostRepository postRepository;

    public EditPost(PostRepository aPostRepository) {
        this.postRepository = aPostRepository;
    }

    public Post handle(EditPostRequest request) throws NullException {
        Post post = postRepository.findByInteger(request.id().intValue());
        
        if (post == null) {
            throw new NullException("Post not found");
        }

        post.update(request.title(), request.content());

        return post;
    }
}

