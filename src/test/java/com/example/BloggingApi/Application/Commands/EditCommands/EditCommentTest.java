package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditCommentRequest;
import com.example.BloggingApi.Domain.Entities.Comment;
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

class EditCommentTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private EditComment editComment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldUpdateComment_WhenCommentExists() throws NullException {
        // Arrange
        EditCommentRequest request = new EditCommentRequest(1L, "Updated content");
        Comment comment = mock(Comment.class);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // Act
        Comment result = editComment.handle(request);

        // Assert
        assertNotNull(result);
        verify(comment).update("Updated content");
    }

    @Test
    void handle_ShouldThrowException_WhenCommentNotFound() {
        // Arrange
        EditCommentRequest request = new EditCommentRequest(1L, "Updated content");
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullException.class, () -> editComment.handle(request));
    }
}