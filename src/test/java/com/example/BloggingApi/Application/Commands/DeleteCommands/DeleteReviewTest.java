package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Services.ReviewService;
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

class DeleteReviewTest {

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
    void handle_ShouldDeleteReview_WhenReviewExists() throws NullException {
        // Arrange
        Review review = mock(Review.class);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // Act
        reviewService.delete(1L);

        // Assert
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void handle_ShouldThrowException_WhenReviewDoesNotExist() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> reviewService.delete(1L));
        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }
}