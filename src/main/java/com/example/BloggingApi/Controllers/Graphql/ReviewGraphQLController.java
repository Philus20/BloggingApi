package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.Entities.Review;
import com.example.BloggingApi.RequestsDTO.CreateReviewRequest;
import com.example.BloggingApi.RequestsDTO.EditReviewRequest;
import com.example.BloggingApi.ResposesDTO.ReviewResponse;
import com.example.BloggingApi.Services.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ReviewGraphQLController {

    private final ReviewService reviewService;

    public ReviewGraphQLController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @QueryMapping
    public Review getReview(@Argument Long id) {
        return reviewService.getReviewById(id);
    }

    @QueryMapping
    public Page<ReviewResponse> listReviews(
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return reviewService.getAllReviews(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<ReviewResponse> searchReviews(
            @Argument String comment,
            @Argument Integer rating,
            @Argument String author,
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return reviewService.search(comment, rating, author, page, size, sortBy, ascending);
    }

    @MutationMapping
    public Review createReview(
            @Argument int rating,
            @Argument String comment,
            @Argument Long userId,
            @Argument Long postId
    ) {
        CreateReviewRequest request = new CreateReviewRequest(rating, comment, userId, postId);
        return reviewService.createReview(request);
    }

    @MutationMapping
    public Review editReview(
            @Argument Long id,
            @Argument int rating,
            @Argument String comment
    ) {
        EditReviewRequest request = new EditReviewRequest(id, rating, comment);
        return reviewService.editReview(request);
    }

    @MutationMapping
    public String deleteReview(@Argument Long id) {
        reviewService.deleteReview(id);
        return "Review with ID " + id + " deleted successfully.";
    }
}
