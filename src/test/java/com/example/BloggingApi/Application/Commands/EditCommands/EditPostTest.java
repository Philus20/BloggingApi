package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.Services.PostService;
import com.example.BloggingApi.DTOs.Requests.EditPostRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EditPostTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private com.example.BloggingApi.Repositories.UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldUpdatePost_WhenPostExists() throws NullException {
        // Arrange
        EditPostRequest request = new EditPostRequest(1L, "New Title", "New Content");
        Post post = mock(Post.class);
        when(postRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(post));

        // Act
        Post result = postService.update(request);

        // Assert
        assertNotNull(result);
        verify(post).update("New Title", "New Content");
    }

    @Test
    void handle_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        EditPostRequest request = new EditPostRequest(1L, "New Title", "New Content");
        when(postRepository.findByIdWithAuthor(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullException.class, () -> postService.update(request));
    }
}