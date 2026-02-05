package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchReviews {

    private final ReviewRepository reviewRepository;

    public SearchReviews(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Search reviews by comment content.
     */
    public Page<Review> searchByComment(String comment, Pageable pageable) {
        return reviewRepository.findByCommentContainingIgnoreCase(comment, pageable);
    }

    /**
     * Search reviews by rating.
     */
    public Page<Review> searchByRating(int rating, Pageable pageable) {
        return reviewRepository.findByRating(rating, pageable);
    }

    /**
     * Search reviews by author username.
     */
    public Page<Review> searchByAuthor(String username, Pageable pageable) {
        return reviewRepository.findByUserUsernameContainingIgnoreCase(username, pageable);
    }
}
