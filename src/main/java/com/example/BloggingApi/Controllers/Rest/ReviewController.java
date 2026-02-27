package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreateReviewRequest;
import com.example.BloggingApi.DTOs.Requests.EditReviewRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.ReviewResponse;
import com.example.BloggingApi.Services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Reviews", description = "Review CRUD and search")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews/{id}")
    @Operation(summary = "Get review by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<ReviewResponse> getReviewById(@Parameter(description = "Review ID") @PathVariable Long id) {
        return ApiResponse.success("Review retrieved successfully", ReviewResponse.from(reviewService.getById(id)));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get all reviews", description = "Paginated list with optional sorting")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameter")
    })
    public ApiResponse<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Reviews retrieved successfully", reviewService.getAll(page, size, sortBy, ascending).map(ReviewResponse::from));
    }

    @GetMapping("/reviews/search")
    @Operation(summary = "Search reviews", description = "Search by comment, rating, or author")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No search parameter provided")
    })
    public ApiResponse<Page<ReviewResponse>> searchReviews(
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Reviews search completed successfully", reviewService.search(comment, rating, author, page, size, sortBy, ascending).map(ReviewResponse::from));
    }

    @PostMapping("/reviews")
    @Operation(summary = "Create a new review")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (rating must be 1-5)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or post not found")
    })
    public ApiResponse<ReviewResponse> createReview(@RequestBody @jakarta.validation.Valid CreateReviewRequest request) {
        return ApiResponse.success("Review created successfully", ReviewResponse.from(reviewService.create(request)));
    }

    @PutMapping("/reviews")
    @Operation(summary = "Update a review")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ApiResponse<ReviewResponse> editReview(@RequestBody @jakarta.validation.Valid EditReviewRequest request) {
        return ApiResponse.success("Review updated successfully", ReviewResponse.from(reviewService.update(request)));
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Delete a review")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<Void> deleteReview(@Parameter(description = "Review ID") @PathVariable Long id) {
        reviewService.delete(id);
        return ApiResponse.success("Review deleted successfully");
    }
}
