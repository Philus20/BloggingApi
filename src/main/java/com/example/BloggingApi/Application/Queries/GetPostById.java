package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class GetPostById {

    private final PostRepository postRepository;

    public GetPostById(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post handle(Long postId) throws NullException {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NullException("Post not found"));
    }
}
