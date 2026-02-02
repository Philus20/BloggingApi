package com.example.BloggingApi.API.Controllers;

import com.example.BloggingApi.API.Requests.CreateReviewRequest;
import com.example.BloggingApi.API.Requests.EditReviewRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.ReviewResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateReview;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteReview;
import com.example.BloggingApi.Application.Commands.EditCommands.EditReview;
import com.example.BloggingApi.Application.Queries.GetAllReviews;
import com.example.BloggingApi.Application.Queries.GetReviewById;
import com.example.BloggingApi.Domain.Entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final CreateReview createReviewHandler;
    private final EditReview editReviewHandler;
    private final DeleteReview deleteReviewHandler;
    private final GetReviewById getReviewByIdHandler;
    private final GetAllReviews getAllReviewsHandler;

    public ReviewController(CreateReview createReviewHandler, EditReview editReviewHandler, DeleteReview deleteReviewHandler, GetReviewById getReviewByIdHandler, GetAllReviews getAllReviewsHandler) {
        this.createReviewHandler = createReviewHandler;
        this.editReviewHandler = editReviewHandler;
        this.deleteReviewHandler = deleteReviewHandler;
        this.getReviewByIdHandler = getReviewByIdHandler;
        this.getAllReviewsHandler = getAllReviewsHandler;
    }

    @GetMapping("/reviews/{id}")
    public ApiResponse<Review> getReviewById(@PathVariable Long id) {
        try {
            Review review = getReviewByIdHandler.handle(id);
            return ApiResponse.success("Review retrieved successfully", review);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @GetMapping("/reviews")
    public ApiResponse<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        try {
            Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Review> reviewsPage = getAllReviewsHandler.handle(pageable);
            Page<ReviewResponse> response = reviewsPage.map(ReviewResponse::from);
            return ApiResponse.success("Reviews retrieved successfully", response);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @PostMapping("/reviews")
    public ApiResponse<Review> createReview(@RequestBody CreateReviewRequest request) {
        try {
            Review review = createReviewHandler.handle(request);
            return ApiResponse.success("Review created successfully", review);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @PutMapping("/reviews")
    public ApiResponse<Review> editReview(@RequestBody EditReviewRequest request) {
        try {
            Review review = editReviewHandler.handle(request);
            return ApiResponse.success("Review updated successfully", review);
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        try {
            deleteReviewHandler.handle(id);
            return ApiResponse.success("Review deleted successfully");
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }
}
