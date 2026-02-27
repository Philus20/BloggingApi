package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateReviewRequest;
import com.example.BloggingApi.DTOs.Requests.EditReviewRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.ReviewRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Utils.PageableUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, PostRepository postRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    @CacheEvict(value = "reviews", allEntries = true)
    public Review create(CreateReviewRequest req) {
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new NullException("User not found"));
        Post post = postRepository.findById(req.postId())
                .orElseThrow(() -> new NullException("Post not found"));
        Review review = Review.create(req.rating(), req.comment(), user, post);
        return reviewRepository.save(review);
    }

    @Transactional
    @CacheEvict(value = "reviews", allEntries = true)
    public Review update(EditReviewRequest request) {
        Review review = reviewRepository.findById(request.id())
                .orElseThrow(() -> new NullException("Review not found"));
        review.update(request.rating(), request.comment());
        return review;
    }

    @Transactional
    @CacheEvict(value = "reviews", allEntries = true)
    public void delete(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NullException("Review not found"));
        reviewRepository.delete(review);
    }

    @Cacheable(value = "reviews", key = "#id")
    public Review getById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NullException("Review not found"));
    }

    public Page<Review> getAll(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    public Page<Review> searchByComment(String comment, Pageable pageable) {
        return reviewRepository.findByCommentContainingIgnoreCase(comment, pageable);
    }

    public Page<Review> searchByRating(int rating, Pageable pageable) {
        return reviewRepository.findByRating(rating, pageable);
    }

    public Page<Review> searchByAuthor(String username, Pageable pageable) {
        return reviewRepository.findByUserUsernameContainingIgnoreCase(username, pageable);
    }

    public Page<Review> search(String comment, Integer rating, String author, Pageable pageable) {
        if (comment != null && !comment.isBlank()) {
            return reviewRepository.findByCommentContainingIgnoreCase(comment, pageable);
        }
        if (rating != null) {
            return reviewRepository.findByRating(rating, pageable);
        }
        if (author != null && !author.isBlank()) {
            return reviewRepository.findByUserUsernameContainingIgnoreCase(author, pageable);
        }
        throw new IllegalArgumentException("Please provide at least one search parameter: comment, rating, or author");
    }

    public Page<Review> getAll(int page, int size, String sortBy, boolean ascending) {
        return getAll(PageableUtils.create(page, size, sortBy, ascending));
    }

    public Page<Review> search(String comment, Integer rating, String author, int page, int size, String sortBy, boolean ascending) {
        return search(comment, rating, author, PageableUtils.create(page, size, sortBy, ascending));
    }

    /** Search with optional criteria; returns empty page when no criteria provided. */
    public Page<Review> searchOptional(String comment, Integer rating, String author, int page, int size, String sortBy, boolean ascending) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, ascending);
        if (comment != null && !comment.isBlank()) {
            return reviewRepository.findByCommentContainingIgnoreCase(comment, pageable);
        }
        if (rating != null) {
            return reviewRepository.findByRating(rating, pageable);
        }
        if (author != null && !author.isBlank()) {
            return reviewRepository.findByUserUsernameContainingIgnoreCase(author, pageable);
        }
        return Page.empty(pageable);
    }
}
