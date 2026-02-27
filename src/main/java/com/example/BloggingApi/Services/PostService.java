package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreatePostRequest;
import com.example.BloggingApi.DTOs.Requests.EditPostRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Utils.PageableUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post create(CreatePostRequest req) {
        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new NullException("Author not found"));
        Post post = Post.create(req.title(), req.content(), author);
        return postRepository.save(post);
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post update(EditPostRequest request) {
        Post post = postRepository.findByIdWithAuthor(request.id())
                .orElseThrow(() -> new NullException("Post not found"));
        post.update(request.title(), request.content());
        return post;
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NullException("Post not found"));
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#id")
    public Post getById(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new NullException("Post not found"));
    }

    @Transactional(readOnly = true)
    public Page<Post> getAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> search(String keyword, String title, String author, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return postRepository.searchByKeyword(keyword, pageable);
        }
        if (title != null && !title.isBlank()) {
            return postRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
        if (author != null && !author.isBlank()) {
            return postRepository.findByAuthorUsernameContainingIgnoreCase(author, pageable);
        }
        throw new IllegalArgumentException("Please provide at least one search parameter: keyword, title, or author");
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts")
    public Page<Post> getAll(int page, int size, String sortBy, boolean ascending) {
        return getAll(PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts")
    public Page<Post> search(String keyword, String title, String author, int page, int size, String sortBy, boolean ascending) {
        return search(keyword, title, author, PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts")
    public Page<Post> searchOptional(String keyword, String title, String author, int page, int size, String sortBy, boolean ascending) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, ascending);
        if (keyword != null && !keyword.isBlank()) {
            return postRepository.searchByKeyword(keyword, pageable);
        }
        if (title != null && !title.isBlank()) {
            return postRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
        if (author != null && !author.isBlank()) {
            return postRepository.findByAuthorUsernameContainingIgnoreCase(author, pageable);
        }
        return Page.empty(pageable);
    }
}
