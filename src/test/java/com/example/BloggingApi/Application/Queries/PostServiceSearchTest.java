package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.PostService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PostServiceSearchTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void search_withKeyword_callsSearchByKeyword() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.searchByKeyword(eq("java"), any())).thenReturn(page);

        Page<Post> result = postService.search("java", null, null, PageRequest.of(0, 5));

        assertNotNull(result);
        verify(postRepository).searchByKeyword(eq("java"), any());
    }

    @Test
    void search_withTitle_callsFindByTitleContaining() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.findByTitleContainingIgnoreCase(eq("Spring"), any())).thenReturn(page);

        Page<Post> result = postService.search(null, "Spring", null, PageRequest.of(0, 5));

        assertNotNull(result);
        verify(postRepository).findByTitleContainingIgnoreCase(eq("Spring"), any());
    }

    @Test
    void search_withAuthor_callsFindByAuthorUsername() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.findByAuthorUsernameContainingIgnoreCase(eq("john"), any())).thenReturn(page);

        Page<Post> result = postService.search(null, null, "john", PageRequest.of(0, 5));

        assertNotNull(result);
        verify(postRepository).findByAuthorUsernameContainingIgnoreCase(eq("john"), any());
    }

    @Test
    void search_withNoParam_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                postService.search(null, null, null, PageRequest.of(0, 5)));
    }

    @Test
    void search_overload_withKeyword_callsSearch() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.searchByKeyword(eq("test"), any())).thenReturn(page);

        Page<Post> result = postService.search("test", null, null, 0, 5, "id", true);

        assertNotNull(result);
        verify(postRepository).searchByKeyword(eq("test"), any());
    }

    @Test
    void searchOptional_withKeyword_returnsPage() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.searchByKeyword(eq("key"), any())).thenReturn(page);

        Page<Post> result = postService.searchOptional("key", null, null, 0, 5, "id", true);

        assertNotNull(result);
        verify(postRepository).searchByKeyword(eq("key"), any());
    }

    @Test
    void searchOptional_withTitle_returnsPage() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.findByTitleContainingIgnoreCase(eq("tit"), any())).thenReturn(page);

        Page<Post> result = postService.searchOptional(null, "tit", null, 0, 5, "id", true);

        assertNotNull(result);
        verify(postRepository).findByTitleContainingIgnoreCase(eq("tit"), any());
    }

    @Test
    void searchOptional_withAuthor_returnsPage() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.findByAuthorUsernameContainingIgnoreCase(eq("auth"), any())).thenReturn(page);

        Page<Post> result = postService.searchOptional(null, null, "auth", 0, 5, "id", true);

        assertNotNull(result);
        verify(postRepository).findByAuthorUsernameContainingIgnoreCase(eq("auth"), any());
    }

    @Test
    void searchOptional_withNoParam_returnsEmptyPage() {
        Page<Post> result = postService.searchOptional(null, null, null, 0, 5, "id", true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(postRepository, never()).searchByKeyword(any(), any());
        verify(postRepository, never()).findByTitleContainingIgnoreCase(any(), any());
        verify(postRepository, never()).findByAuthorUsernameContainingIgnoreCase(any(), any());
    }

    @Test
    void getAll_withPageSize_callsFindAll() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Post> result = postService.getAll(0, 5, "id", true);

        assertNotNull(result);
        verify(postRepository).findAll(any(Pageable.class));
    }
}
