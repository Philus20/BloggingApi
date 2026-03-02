package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.ReviewRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.ReviewService;
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

class ReviewServiceSearchTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void search_withComment_callsFindByComment() {
        Page<Review> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findByCommentContainingIgnoreCase(eq("good"), any())).thenReturn(page);

        Page<Review> result = reviewService.search("good", null, null, PageRequest.of(0, 5));

        assertNotNull(result);
        verify(reviewRepository).findByCommentContainingIgnoreCase(eq("good"), any());
    }

    @Test
    void search_withRating_callsFindByRating() {
        Page<Review> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findByRating(eq(5), any())).thenReturn(page);

        Page<Review> result = reviewService.search(null, 5, null, PageRequest.of(0, 5));

        assertNotNull(result);
        verify(reviewRepository).findByRating(eq(5), any());
    }

    @Test
    void search_withAuthor_callsFindByUserUsername() {
        Page<Review> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findByUserUsernameContainingIgnoreCase(eq("alice"), any())).thenReturn(page);

        Page<Review> result = reviewService.search(null, null, "alice", PageRequest.of(0, 5));

        assertNotNull(result);
        verify(reviewRepository).findByUserUsernameContainingIgnoreCase(eq("alice"), any());
    }

    @Test
    void search_withNoParam_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                reviewService.search(null, null, null, PageRequest.of(0, 5)));
    }

    @Test
    void searchOptional_withComment_returnsPage() {
        Page<Review> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findByCommentContainingIgnoreCase(eq("nice"), any())).thenReturn(page);

        Page<Review> result = reviewService.searchOptional("nice", null, null, 0, 5, "id", true);

        assertNotNull(result);
        verify(reviewRepository).findByCommentContainingIgnoreCase(eq("nice"), any());
    }

    @Test
    void getAll_withPageSize_callsFindAll() {
        Page<Review> page = new PageImpl<>(Collections.emptyList());
        when(reviewRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Review> result = reviewService.getAll(0, 5, "id", true);

        assertNotNull(result);
        verify(reviewRepository).findAll(any(Pageable.class));
    }
}
