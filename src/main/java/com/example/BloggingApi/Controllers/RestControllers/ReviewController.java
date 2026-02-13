package com.example.BloggingApi.Controllers.RestControllers;

import com.example.BloggingApi.RequestsDTO.CreateReviewRequest;
import com.example.BloggingApi.RequestsDTO.EditReviewRequest;
import com.example.BloggingApi.ResposesDTO.ApiResponse;
import com.example.BloggingApi.ResposesDTO.ReviewResponse;
import com.example.BloggingApi.Services.ReviewService;
import com.example.BloggingApi.Entities.Review;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@Tag(name = "Reviews", description = "Create, read, update, and delete reviews on posts")
public class ReviewController {

    private final ReviewService reviewServiceHandler;

    public ReviewController(ReviewService reviewServiceHandler) {
        this.reviewServiceHandler = reviewServiceHandler;
    }


    @Operation(summary = "Get review by ID")
    @GetMapping("/reviews/{id}")
    public ApiResponse<ReviewResponse> getReviewById(@Parameter(description = "Review ID") @PathVariable Long id) {
        Review review = reviewServiceHandler.getReviewById(id);
        return ApiResponse.success("Review retrieved successfully", ReviewResponse.from(review));
    }

    @Operation(summary = "List all reviews", description = "Paginated and sorted list of reviews")
    @GetMapping("/reviews")
    public ApiResponse<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success(
                "Reviews retrieved successfully",
                reviewServiceHandler.getAllReviews(page, size, sortBy, ascending)
        );
    }



    @Operation(summary = "Search reviews", description = "Search by comment, rating, or author. At least one parameter required.")
    @GetMapping("/reviews/search")
    public ApiResponse<Page<ReviewResponse>> searchReviews(
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success(
                "Reviews search completed successfully",
                reviewServiceHandler.search(
                        comment, rating, author, page, size, sortBy, ascending
                )
        );
    }

    @Operation(summary = "Create review", description = "Requires rating, userId, and postId")
    @PostMapping("/reviews")
    public ApiResponse<ReviewResponse> createReview(@RequestBody @Valid CreateReviewRequest request) {
        Review review = reviewServiceHandler.createReview(request);
        return ApiResponse.success("Review created successfully", ReviewResponse.from(review));
    }

    @Operation(summary = "Update review")
    @PutMapping("/reviews")
    public ApiResponse<ReviewResponse> editReview(@RequestBody @jakarta.validation.Valid EditReviewRequest request) {
        Review review = reviewServiceHandler.editReview(request);
        return ApiResponse.success("Review updated successfully", ReviewResponse.from(review));
    }

    @Operation(summary = "Delete review")
    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteReview(@Parameter(description = "Review ID") @PathVariable Long id) {
        reviewServiceHandler.deleteReview(id);
        return ApiResponse.success("Review deleted successfully");
    }
}
