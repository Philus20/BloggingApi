package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GetAllReviews {

    private final ReviewRepository reviewRepository;

    public GetAllReviews(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Page<Review> handle(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }
}

