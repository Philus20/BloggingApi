package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateCommentRequest;
import com.example.BloggingApi.Domain.Entities.Comment;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;

import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.CommentRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.PostRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateCommentTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateComment createComment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldCreateComment_WhenRequestIsValid() throws NullException {
        // Arrange
        CreateCommentRequest req = new CreateCommentRequest("Test Comment", 1L, 1L);
        Post post = mock(Post.class);
        User author = mock(User.class);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        // Act
        Comment result = createComment.handle(req);

        // Assert
        assertNotNull(result);
        assertEquals("Test Comment", result.getContent());
        verify(commentRepository, times(1)).create(any(Comment.class));
    }

    @Test
    void handle_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        CreateCommentRequest req = new CreateCommentRequest("Test Comment", 1L, 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> createComment.handle(req));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void handle_ShouldThrowException_WhenAuthorNotFound() {
        // Arrange
        CreateCommentRequest req = new CreateCommentRequest("Test Comment", 1L, 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(mock(Post.class)));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NullException exception = assertThrows(NullException.class, () -> createComment.handle(req));
        assertEquals("Author not found", exception.getMessage());
    }
}