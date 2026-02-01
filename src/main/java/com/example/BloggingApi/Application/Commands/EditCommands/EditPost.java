package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditPostRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Domain.Repositories.IRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EditPost {

    private final PostRepository postRepository;

    public EditPost(PostRepository aPostRepository) {
        this.postRepository = aPostRepository;
    }

    @Transactional
    public Post handle( EditPostRequest request) throws NullException {
        Post post = postRepository.findById(request.id())
                .orElseThrow(() -> new NullException("Post not found"));

        post.update(request.title(), request.content());

        return post;
    }
}

