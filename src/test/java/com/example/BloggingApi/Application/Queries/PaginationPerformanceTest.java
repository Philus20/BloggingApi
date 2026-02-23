package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests pagination and sorting behavior for DSA Integration (Technical Requirement #10).
 * Verifies that Pageable is correctly built and passed to the repository for DB-level execution.
 */
class PaginationPerformanceTest {

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
    void getAll_ShouldPassPageableWithCorrectSortToRepository() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        postService.getAll(1, 10, "createdAt", false);

        verify(postRepository).findAll(pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();

        assertEquals(1, captured.getPageNumber());
        assertEquals(10, captured.getPageSize());
        Sort.Order order = captured.getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getAll_ShouldReturnPaginatedResult() {
        Page<Post> expectedPage = new PageImpl<>(Collections.emptyList(), Pageable.ofSize(5), 0);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Post> result = postService.getAll(0, 5, "id", true);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(5, result.getSize());
    }

    @Test
    void search_ShouldDelegatePageableToRepository() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(postRepository.searchByKeyword(eq("test"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        postService.search("test", null, null, 0, 20, "title", true);

        verify(postRepository).searchByKeyword(eq("test"), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(20, captured.getPageSize());
        assertEquals(Sort.Direction.ASC, captured.getSort().getOrderFor("title").getDirection());
    }

    @Test
    void pagination_ShouldCompleteWithinReasonableTime() {
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        long start = System.nanoTime();
        postService.getAll(0, 10, "id", true);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // With mocked repository, call should be near-instantaneous.
        // This documents the expectation that pagination does not load full result sets.
        assertTrue(elapsedMs < 100, "Pagination should delegate to repository without significant overhead");
    }
}
