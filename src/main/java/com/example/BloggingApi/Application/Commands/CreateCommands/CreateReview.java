package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateReviewRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.ReviewRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateReview {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CreateReview(ReviewRepository reviewRepository, UserRepository userRepository, PostRepository postRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public Review handle(CreateReviewRequest req) throws NullException {
        // Validation
        if (req.rating() < 1 || req.rating() > 5) {
            throw new NullException("Rating must be between 1 and 5");
        }
        if (req.userId() == null) {
            throw new NullException("User ID cannot be null");
        }
        if (req.postId() == null) {
            throw new NullException("Post ID cannot be null");
        }

        // Fetch User entity
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new NullException("User not found"));

        // Fetch Post entity
        Post post = postRepository.findById(req.postId())
                .orElseThrow(() -> new NullException("Post not found"));

        // Create Review entity
        Review review = Review.create(req.rating(), req.comment(), user, post);

        // Save to DB
        reviewRepository.save(review);

        return review;
    }
}
