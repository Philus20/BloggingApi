package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Services.CommentService;
import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteCommentTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private com.example.BloggingApi.Repositories.PostRepository postRepository;

    @Mock
    private com.example.BloggingApi.Repositories.UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldDeleteComment_WhenCommentExists() throws NullException {
        // Arrange
        Comment comment = mock(Comment.class);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // Act
        commentService.delete(1L);

        // Assert
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void handle_ShouldThrowException_WhenCommentDoesNotExist() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> commentService.delete(1L));
        assertEquals("Comment not found", exception.getMessage());
        verify(commentRepository, never()).delete(any());
    }
}