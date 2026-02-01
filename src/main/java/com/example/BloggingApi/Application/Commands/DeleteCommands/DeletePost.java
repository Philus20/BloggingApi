package com.example.BloggingApi.Application.Commands.DeleteCommands;


import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeletePost {

    private final PostRepository postRepository;

    public DeletePost(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public void handle(Long postId) throws NullException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NullException("Post not found"));

        postRepository.delete(post);
    }
}

