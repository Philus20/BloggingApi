package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.Services.CommentService;
import com.example.BloggingApi.DTOs.Requests.CreateCommentRequest;
import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCommentTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void handle_ShouldCreateComment_WhenRequestIsValid() throws NullException {
        // Arrange
        CreateCommentRequest req = new CreateCommentRequest("Test Comment", 1L, 1L);
        Post post = mock(Post.class);
        User author = mock(User.class);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Comment result = commentService.create(req);

        // Assert
        assertNotNull(result);
        assertEquals("Test Comment", result.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void handle_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        CreateCommentRequest req = new CreateCommentRequest("Test Comment", 1L, 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> commentService.create(req));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void handle_ShouldThrowException_WhenAuthorNotFound() {
        // Arrange
        CreateCommentRequest req = new CreateCommentRequest("Test Comment", 1L, 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> commentService.create(req));
        assertEquals("Author not found", exception.getMessage());
    }
}