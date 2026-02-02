package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteReview {

    private final ReviewRepository reviewRepository;

    public DeleteReview(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public void handle(Long reviewId) throws NullException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NullException("Review not found"));

        reviewRepository.delete(review);
    }
}
