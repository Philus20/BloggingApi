package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class GetReviewById {

    private final ReviewRepository reviewRepository;

    public GetReviewById(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review handle(Long reviewId) throws NullException {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NullException("Review not found"));
    }
}
