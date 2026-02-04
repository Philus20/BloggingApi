package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditPostRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.PostRepository;
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

    @InjectMocks
    private EditPost editPost;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldUpdatePost_WhenPostExists() throws NullException {
        // Arrange
        EditPostRequest request = new EditPostRequest(1L, "New Title", "New Content");
        Post post = mock(Post.class);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        Post result = editPost.handle(request);

        // Assert
        assertNotNull(result);
        verify(post).update("New Title", "New Content");
    }

    @Test
    void handle_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        EditPostRequest request = new EditPostRequest(1L, "New Title", "New Content");
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullException.class, () -> editPost.handle(request));
    }
}