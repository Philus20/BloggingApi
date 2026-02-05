package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Post;
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

class GetPostByIdTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private GetPostById getPostById;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldReturnPost_WhenPostExists() throws NullException {
        // Arrange
        Post post = mock(Post.class);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        Post result = getPostById.handle(1L);

        // Assert
        assertNotNull(result);
        assertEquals(post, result);
    }

    @Test
    void handle_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullException.class, () -> getPostById.handle(1L));
    }
}