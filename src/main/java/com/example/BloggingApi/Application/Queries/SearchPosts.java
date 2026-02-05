package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchPosts {

    private final PostRepository postRepository;

    public SearchPosts(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Search posts by keyword in title or content
     * Uses database indexes for efficient searching
     */
    public Page<Post> searchByKeyword(String keyword, Pageable pageable) {
        return postRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * Search posts by title only
     * Uses idx_post_title index
     */
    public Page<Post> searchByTitle(String title, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    /**
     * Search posts by author username
     */
    public Page<Post> searchByAuthor(String username, Pageable pageable) {
        return postRepository.findByAuthorUsernameContainingIgnoreCase(username, pageable);
    }
}
