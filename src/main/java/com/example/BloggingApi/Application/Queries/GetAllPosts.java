package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllPosts {

    private final PostRepository postRepository;

    public GetAllPosts(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> handle(Pageable pageable) {
        return postRepository.findAll(pageable);
    }


}
