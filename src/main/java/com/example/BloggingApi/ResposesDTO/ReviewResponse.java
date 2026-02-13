package com.example.BloggingApi.ResposesDTO;

import com.example.BloggingApi.Entities.Review;

public record ReviewResponse(
        Long id,
        int rating,
        String comment,
        String username,
        Long postId
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getUsername(),
                review.getPost().getId()
        );
    }
}
