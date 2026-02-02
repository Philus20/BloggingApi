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
import com.example.BloggingApi.Application.Queries.SearchReviews;
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
    private final SearchReviews searchReviewsHandler;

    public ReviewController(CreateReview createReviewHandler, EditReview editReviewHandler, DeleteReview deleteReviewHandler, GetReviewById getReviewByIdHandler, GetAllReviews getAllReviewsHandler, SearchReviews searchReviewsHandler) {
        this.createReviewHandler = createReviewHandler;
        this.editReviewHandler = editReviewHandler;
        this.deleteReviewHandler = deleteReviewHandler;
        this.getReviewByIdHandler = getReviewByIdHandler;
        this.getAllReviewsHandler = getAllReviewsHandler;
        this.searchReviewsHandler = searchReviewsHandler;
    }

    @GetMapping("/reviews/{id}")
    public ApiResponse<ReviewResponse> getReviewById(@PathVariable Long id) {
        Review review = getReviewByIdHandler.handle(id);
        return ApiResponse.success("Review retrieved successfully", ReviewResponse.from(review));
    }

    @GetMapping("/reviews")
    public ApiResponse<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewsPage = getAllReviewsHandler.handle(pageable);
        Page<ReviewResponse> response = reviewsPage.map(ReviewResponse::from);
        return ApiResponse.success("Reviews retrieved successfully", response);
    }



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
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewsPage;

        if (comment != null && !comment.isBlank()) {
            reviewsPage = searchReviewsHandler.searchByComment(comment, pageable);
        } else if (rating != null) {
            reviewsPage = searchReviewsHandler.searchByRating(rating, pageable);
        } else if (author != null && !author.isBlank()) {
            reviewsPage = searchReviewsHandler.searchByAuthor(author, pageable);
        } else {
            throw new IllegalArgumentException("Please provide at least one search parameter: comment, rating, or author");
        }

        Page<ReviewResponse> response = reviewsPage.map(ReviewResponse::from);
        return ApiResponse.success("Reviews search completed successfully", response);
    }

    @PostMapping("/reviews")
    public ApiResponse<ReviewResponse> createReview(@RequestBody @jakarta.validation.Valid CreateReviewRequest request) {
        Review review = createReviewHandler.handle(request);
        return ApiResponse.success("Review created successfully", ReviewResponse.from(review));
    }

    @PutMapping("/reviews")
    public ApiResponse<ReviewResponse> editReview(@RequestBody @jakarta.validation.Valid EditReviewRequest request) {
        Review review = editReviewHandler.handle(request);
        return ApiResponse.success("Review updated successfully", ReviewResponse.from(review));
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        deleteReviewHandler.handle(id);
        return ApiResponse.success("Review deleted successfully");
    }
}
