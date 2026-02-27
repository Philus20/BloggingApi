package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateCommentRequest;
import com.example.BloggingApi.DTOs.Requests.EditCommentRequest;
import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Utils.PageableUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public Comment create(CreateCommentRequest req) {
        Post post = postRepository.findById(req.postId())
                .orElseThrow(() -> new NullException("Post not found"));
        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new NullException("Author not found"));
        Comment comment = Comment.create(req.content(), post, author);
        return commentRepository.save(comment);
    }

    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public Comment update(EditCommentRequest request) {
        Comment comment = commentRepository.findByIdWithRelations(request.id())
                .orElseThrow(() -> new NullException("Comment not found"));
        comment.update(request.content());
        return comment;
    }

    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NullException("Comment not found"));
        commentRepository.delete(comment);
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#id")
    public Comment getById(Long id) {
        return commentRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new NullException("Comment not found"));
    }

    @Transactional(readOnly = true)
    public Page<Comment> getAll(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Comment> search(String content, String author, Pageable pageable) {
        if (content != null && !content.isBlank()) {
            return commentRepository.findByContentContainingIgnoreCase(content, pageable);
        }
        if (author != null && !author.isBlank()) {
            return commentRepository.findByAuthorUsernameContainingIgnoreCase(author, pageable);
        }
        throw new IllegalArgumentException("Please provide at least one search parameter: content or author");
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments")
    public Page<Comment> getAll(int page, int size, String sortBy, boolean ascending) {
        return getAll(PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments")
    public Page<Comment> search(String content, String author, int page, int size, String sortBy, boolean ascending) {
        return search(content, author, PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments")
    public Page<Comment> searchOptional(String content, String author, int page, int size, String sortBy, boolean ascending) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, ascending);
        if (content != null && !content.isBlank()) {
            return commentRepository.findByContentContainingIgnoreCase(content, pageable);
        }
        if (author != null && !author.isBlank()) {
            return commentRepository.findByAuthorUsernameContainingIgnoreCase(author, pageable);
        }
        return Page.empty(pageable);
    }
}
