package com.example.BloggingApi.Services;

import com.example.BloggingApi.Entities.Comment;
import com.example.BloggingApi.Entities.Post;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.CreateCommentRequest;
import com.example.BloggingApi.RequestsDTO.EditCommentRequest;
import com.example.BloggingApi.ResposesDTO.CommentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServicesTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServices commentServices;

    private User author;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = User.create("john", "john@example.com", "pwd");
        post = Post.create("Title", "Content", author);
        comment = Comment.create("Nice post", post, author);
    }

    @Test
    void getCommentById_returnsMappedResponse_whenFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        CommentResponse result = commentServices.getCommentById(1L);

        assertThat(result.id()).isEqualTo(comment.getId());
        assertThat(result.content()).isEqualTo("Nice post");
        verify(commentRepository).findById(1L);
    }

    @Test
    void getCommentById_throwsNullException_whenMissing() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentServices.getCommentById(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void getAllComments_returnsPageOfResponses() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Comment> page = new PageImpl<>(List.of(comment), pageable, 1);
        when(commentRepository.findAll(any())).thenReturn(page);

        Page<CommentResponse> result = commentServices.getAllComments(0, 5, "id", true);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(commentRepository).findAll(any());
    }

    @Test
    void searchComments_usesContentSearch_whenContentProvided() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Comment> page = new PageImpl<>(List.of(comment), pageable, 1);
        when(commentRepository.findByContentContainingIgnoreCase(eq("nice"), any()))
                .thenReturn(page);

        Page<CommentResponse> result = commentServices.searchComments(
                "nice", null, 0, 5, "createdAt", false);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(commentRepository).findByContentContainingIgnoreCase(eq("nice"), any());
        verify(commentRepository, never()).findByAuthorUsernameContainingIgnoreCase(anyString(), any());
    }

    @Test
    void searchComments_throwsIllegalArgument_whenBothParamsBlank() {
        assertThatThrownBy(() -> commentServices.searchComments(
                " ", " ", 0, 5, "id", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provide at least content or author");
    }

    @Test
    void createComment_createsAndReturnsResponse_whenPostAndAuthorExist() {
        CreateCommentRequest request = new CreateCommentRequest("Nice", 2L, 3L);
        when(postRepository.findById(2L)).thenReturn(Optional.of(post));
        when(userRepository.findById(3L)).thenReturn(Optional.of(author));

        CommentResponse result = commentServices.createComment(request);

        assertThat(result.content()).isEqualTo("Nice");
        verify(postRepository).findById(2L);
        verify(userRepository).findById(3L);
        verify(commentRepository).create(any(Comment.class));
    }

    @Test
    void createComment_throwsNullException_whenPostMissing() {
        CreateCommentRequest request = new CreateCommentRequest("Nice", 2L, 3L);
        when(postRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentServices.createComment(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void editComment_updatesExistingComment() {
        EditCommentRequest request = new EditCommentRequest(1L, "Updated");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        CommentResponse result = commentServices.editComment(request);

        assertThat(result.content()).isEqualTo("Updated");
        verify(commentRepository).findById(1L);
    }

    @Test
    void deleteComment_deletes_whenExists() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentServices.deleteComment(1L);

        verify(commentRepository).findById(1L);
        verify(commentRepository).delete(1);
    }
}
