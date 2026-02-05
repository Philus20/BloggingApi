package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.*;
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

    @InjectMocks
    private DeleteReview deleteReview;

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
        deleteReview.handle(1L);

        // Assert
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void handle_ShouldThrowException_WhenReviewDoesNotExist() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> deleteReview.handle(1L));
        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }
}