package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.DTOs.Requests.CreateReviewRequest;
import com.example.BloggingApi.DTOs.Requests.EditReviewRequest;
import com.example.BloggingApi.Services.ReviewService;
import com.example.BloggingApi.Domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class ReviewGraphQLController {

    private final ReviewService reviewService;

    public ReviewGraphQLController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    public Review getReview(@Argument Long id) {
        return reviewService.getById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    public Page<Review> listReviews(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return reviewService.getAll(page, size, sortBy, ascending);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    public Page<Review> searchReviews(@Argument String comment, @Argument Integer rating, @Argument String author,
                                      @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return reviewService.searchOptional(comment, rating, author, page, size, sortBy, ascending);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public Review createReview(@Argument int rating, @Argument String comment, @Argument Long userId, @Argument Long postId) {
        return reviewService.create(new CreateReviewRequest(rating, comment, userId, postId));
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public Review editReview(@Argument Long id, @Argument int rating, @Argument String comment) {
        return reviewService.update(new EditReviewRequest(id, rating, comment));
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public String deleteReview(@Argument Long id) {
        reviewService.delete(id);
        return "Review with ID " + id + " deleted successfully.";
    }
}
