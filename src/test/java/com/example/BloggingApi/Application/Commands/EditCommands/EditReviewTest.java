package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditReviewRequest;
import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.ReviewRepository;
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

    @InjectMocks
    private EditReview editReview;

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
        Review result = editReview.handle(request);

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
        assertThrows(NullException.class, () -> editReview.handle(request));
    }

    @Test
    void handle_ShouldThrowException_WhenRatingIsInvalid() {
        // Arrange
        EditReviewRequest request = new EditReviewRequest(1L, 6, "Invalid rating");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(mock(Review.class)));

        // Act & Assert
        assertThrows(NullException.class, () -> editReview.handle(request));
    }
}
