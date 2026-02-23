package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Repositories.ReviewRepository;
import com.example.BloggingApi.Services.ReviewService;
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
    void handle_ShouldReturnPageOfReviews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);

        // Act
        Page<Review> result = reviewService.getAll(pageable);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).findAll(pageable);
    }
}