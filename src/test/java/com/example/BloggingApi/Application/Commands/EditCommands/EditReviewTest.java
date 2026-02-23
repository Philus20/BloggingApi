package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.Services.ReviewService;
import com.example.BloggingApi.DTOs.Requests.EditReviewRequest;
import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EditReviewTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private com.example.BloggingApi.Repositories.UserRepository userRepository;

    @Mock
    private com.example.BloggingApi.Repositories.PostRepository postRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldUpdateReview_WhenReviewExists() throws NullException {
        // Arrange
        EditReviewRequest request = new EditReviewRequest(1L, 5, "Updated feedback");
        Review review = mock(Review.class);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // Act
        Review result = reviewService.update(request);

        // Assert
        assertNotNull(result);
        verify(review).update(5, "Updated feedback");
    }

    @Test
    void handle_ShouldThrowException_WhenReviewNotFound() {
        // Arrange
        EditReviewRequest request = new EditReviewRequest(1L, 5, "Updated feedback");
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullException.class, () -> reviewService.update(request));
    }

    // Rating range is validated at request level via @Min(1) @Max(5)
}
