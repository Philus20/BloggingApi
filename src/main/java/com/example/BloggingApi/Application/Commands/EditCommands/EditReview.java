package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditReviewRequest;
import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EditReview {

    private final ReviewRepository reviewRepository;

    public EditReview(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Review handle(EditReviewRequest request) throws NullException {
        Review review = reviewRepository.findById(request.id())
                .orElseThrow(() -> new NullException("Review not found"));

        if (request.rating() < 1 || request.rating() > 5) {
            throw new NullException("Rating must be between 1 and 5");
        }

        review.update(request.rating(), request.comment());

        return review;
    }
}
