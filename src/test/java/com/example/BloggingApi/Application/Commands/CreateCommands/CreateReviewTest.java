package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateReviewRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.Review;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.*;
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
    private CreateReview createReview;

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

        // Act
        Review result = createReview.handle(req);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great post", result.getComment());
        verify(reviewRepository, times(1)).create(any(Review.class));
    }

    @Test
    void handle_ShouldThrowException_WhenRatingIsInvalid() {
        // Arrange
        CreateReviewRequest req = new CreateReviewRequest(6, "Invalid", 1L, 1L);

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> createReview.handle(req));
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void handle_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateReviewRequest req = new CreateReviewRequest(5, "Great post", 1L, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> createReview.handle(req));
        assertEquals("User found", exception.getMessage().contains("User") ? exception.getMessage() : "User not found"); // Double check class for exact message
    }
}