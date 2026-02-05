package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAllReviewsTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private GetAllReviews getAllReviews;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldReturnPageOfReviews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);

        // Act
        Page<Review> result = getAllReviews.handle(pageable);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).findAll(pageable);
    }
}