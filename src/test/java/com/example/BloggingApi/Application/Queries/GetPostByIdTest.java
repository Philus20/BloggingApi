package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.PostService;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldReturnPost_WhenPostExists() throws NullException {
        // Arrange
        Post post = mock(Post.class);
        when(postRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(post));

        // Act
        Post result = postService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(post, result);
    }

    @Test
    void handle_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        when(postRepository.findByIdWithAuthor(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullException.class, () -> postService.getById(1L));
    }
}