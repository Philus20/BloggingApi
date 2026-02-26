package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateCommentRequest;
import com.example.BloggingApi.DTOs.Requests.EditCommentRequest;
import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private Post post;
    private User author;
    private Comment comment;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        author = User.create("author", "a@example.com", "pass");
        post = Post.create("Post Title", "Post content", author);
        comment = Comment.create("Comment text", post, author);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_shouldSaveAndReturnComment() {
        CreateCommentRequest req = new CreateCommentRequest("New comment", 1L, 2L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = commentService.create(req);

        assertNotNull(result);
        assertEquals("New comment", result.getContent());
        verify(postRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void create_shouldThrowWhenPostNotFound() {
        CreateCommentRequest req = new CreateCommentRequest("C", 999L, 1L);
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> commentService.create(req));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenAuthorNotFound() {
        CreateCommentRequest req = new CreateCommentRequest("C", 1L, 999L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> commentService.create(req));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnComment() {
        EditCommentRequest req = new EditCommentRequest(1L, "Updated content");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment result = commentService.update(req);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        verify(commentRepository).findById(1L);
    }

    @Test
    void update_shouldThrowWhenCommentNotFound() {
        EditCommentRequest req = new EditCommentRequest(999L, "C");
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> commentService.update(req));
    }

    @Test
    void delete_shouldDeleteComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);

        commentService.delete(1L);

        verify(commentRepository).findById(1L);
        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_shouldThrowWhenCommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> commentService.delete(999L));
    }

    @Test
    void getById_shouldReturnComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment result = commentService.getById(1L);

        assertNotNull(result);
        assertEquals("Comment text", result.getContent());
        verify(commentRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> commentService.getById(999L));
    }

    @Test
    void getAll_withPageable_shouldReturnPage() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findAll(pageable)).thenReturn(page);

        Page<Comment> result = commentService.getAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(commentRepository).findAll(pageable);
    }

    @Test
    void getAll_withParams_shouldDelegate() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Comment> result = commentService.getAll(0, 5, "id", true);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_byContent_shouldCallFindByContent() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findByContentContainingIgnoreCase("text", pageable)).thenReturn(page);

        Page<Comment> result = commentService.search("text", null, pageable);

        assertEquals(1, result.getContent().size());
        verify(commentRepository).findByContentContainingIgnoreCase("text", pageable);
    }

    @Test
    void search_byAuthor_shouldCallFindByAuthor() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findByAuthorUsernameContainingIgnoreCase("auth", pageable)).thenReturn(page);

        Page<Comment> result = commentService.search(null, "auth", pageable);

        assertEquals(1, result.getContent().size());
        verify(commentRepository).findByAuthorUsernameContainingIgnoreCase("auth", pageable);
    }

    @Test
    void search_withNoParams_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> commentService.search(null, null, pageable));
    }

    @Test
    void searchOptional_withContent_shouldReturnPage() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findByContentContainingIgnoreCase(eq("c"), any(Pageable.class))).thenReturn(page);

        Page<Comment> result = commentService.searchOptional("c", null, 0, 5, "id", true);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchOptional_withNoParams_shouldReturnEmptyPage() {
        Page<Comment> result = commentService.searchOptional(null, null, 0, 5, "id", true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchByContent_shouldDelegate() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findByContentContainingIgnoreCase("q", pageable)).thenReturn(page);

        Page<Comment> result = commentService.searchByContent("q", pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchByAuthor_shouldDelegate() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findByAuthorUsernameContainingIgnoreCase("a", pageable)).thenReturn(page);

        Page<Comment> result = commentService.searchByAuthor("a", pageable);

        assertEquals(1, result.getContent().size());
    }
}
