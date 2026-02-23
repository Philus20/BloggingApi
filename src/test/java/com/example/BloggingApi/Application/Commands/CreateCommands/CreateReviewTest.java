package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.Services.ReviewService;
import com.example.BloggingApi.DTOs.Requests.CreateReviewRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.ReviewRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateReviewTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldCreateReview_WhenRequestIsValid() throws NullException {
        // Arrange
        CreateReviewRequest req = new CreateReviewRequest(5, "Great post", 1L, 1L);
        User user = mock(User.class);
        Post post = mock(Post.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Review result = reviewService.create(req);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great post", result.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    // Rating range is validated at request level via @Min(1) @Max(5)

    @Test
    void handle_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateReviewRequest req = new CreateReviewRequest(5, "Great post", 1L, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> reviewService.create(req));
        assertEquals("User not found", exception.getMessage());
    }
}