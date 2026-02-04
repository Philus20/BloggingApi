package com.example.BloggingApi.Application.Commands.DeleteCommands;

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

class DeletePostTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private DeletePost deletePost;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldDeletePost_WhenPostExists() throws NullException {
        // Arrange
        Post post = mock(Post.class);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        deletePost.handle(1L);

        // Assert
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void handle_ShouldThrowException_WhenPostDoesNotExist() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> deletePost.handle(1L));
        assertEquals("Post not found", exception.getMessage());
        verify(postRepository, never()).delete(any());
    }
}