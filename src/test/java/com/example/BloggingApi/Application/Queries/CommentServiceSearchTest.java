package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentServiceSearchTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }

    @Test
    void search_withContent_callsFindByContent() {
        Page<Comment> page = new PageImpl<>(Collections.emptyList());
        when(commentRepository.findByContentContainingIgnoreCase(eq("hello"), any())).thenReturn(page);

        Page<Comment> result = commentService.search("hello", null, PageRequest.of(0, 5));

        assertNotNull(result);
        verify(commentRepository).findByContentContainingIgnoreCase(eq("hello"), any());
    }

    @Test
    void search_withAuthor_callsFindByAuthorUsername() {
        Page<Comment> page = new PageImpl<>(Collections.emptyList());
        when(commentRepository.findByAuthorUsernameContainingIgnoreCase(eq("john"), any())).thenReturn(page);

        Page<Comment> result = commentService.search(null, "john", PageRequest.of(0, 5));

        assertNotNull(result);
        verify(commentRepository).findByAuthorUsernameContainingIgnoreCase(eq("john"), any());
    }

    @Test
    void search_withNoParam_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                commentService.search(null, null, PageRequest.of(0, 5)));
    }

    @Test
    void search_overload_callsSearch() {
        Page<Comment> page = new PageImpl<>(Collections.emptyList());
        when(commentRepository.findByContentContainingIgnoreCase(eq("x"), any())).thenReturn(page);

        Page<Comment> result = commentService.search("x", null, 0, 5, "id", true);

        assertNotNull(result);
        verify(commentRepository).findByContentContainingIgnoreCase(eq("x"), any());
    }

    @Test
    void getAll_withPageSize_callsFindAll() {
        Page<Comment> page = new PageImpl<>(Collections.emptyList());
        when(commentRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Comment> result = commentService.getAll(0, 5, "id", true);

        assertNotNull(result);
        verify(commentRepository).findAll(any(Pageable.class));
    }
}
