package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateReviewRequest;
import com.example.BloggingApi.DTOs.Requests.EditReviewRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.ReviewRepository;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Post post;
    private Review review;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = User.create("user", "u@example.com", "pass");
        post = Post.create("Post", "Content", user);
        review = Review.create(5, "Great post", user, post);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_shouldSaveAndReturnReview() {
        CreateReviewRequest req = new CreateReviewRequest(4, "Good", 1L, 2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(2L)).thenReturn(Optional.of(post));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review result = reviewService.create(req);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Good", result.getComment());
        verify(userRepository).findById(1L);
        verify(postRepository).findById(2L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        CreateReviewRequest req = new CreateReviewRequest(3, "Ok", 999L, 1L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> reviewService.create(req));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenPostNotFound() {
        CreateReviewRequest req = new CreateReviewRequest(3, "Ok", 1L, 999L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> reviewService.create(req));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnReview() {
        EditReviewRequest req = new EditReviewRequest(1L, 3, "Updated");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Review result = reviewService.update(req);

        assertNotNull(result);
        assertEquals(3, result.getRating());
        assertEquals("Updated", result.getComment());
        verify(reviewRepository).findById(1L);
    }

    @Test
    void update_shouldThrowWhenReviewNotFound() {
        EditReviewRequest req = new EditReviewRequest(999L, 3, "C");
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> reviewService.update(req));
    }

    @Test
    void delete_shouldDeleteReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.delete(1L);

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).delete(review);
    }

    @Test
    void delete_shouldThrowWhenReviewNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> reviewService.delete(999L));
    }

    @Test
    void getById_shouldReturnReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Review result = reviewService.getById(1L);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        verify(reviewRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> reviewService.getById(999L));
    }

    @Test
    void getAll_withPageable_shouldReturnPage() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findAll(pageable)).thenReturn(page);

        Page<Review> result = reviewService.getAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(reviewRepository).findAll(pageable);
    }

    @Test
    void getAll_withParams_shouldDelegate() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Review> result = reviewService.getAll(0, 5, "id", true);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_byComment_shouldCallFindByComment() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByCommentContainingIgnoreCase("Great", pageable)).thenReturn(page);

        Page<Review> result = reviewService.search("Great", null, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(reviewRepository).findByCommentContainingIgnoreCase("Great", pageable);
    }

    @Test
    void search_byRating_shouldCallFindByRating() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByRating(5, pageable)).thenReturn(page);

        Page<Review> result = reviewService.search(null, 5, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(reviewRepository).findByRating(5, pageable);
    }

    @Test
    void search_byAuthor_shouldCallFindByUserUsername() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByUserUsernameContainingIgnoreCase("user", pageable)).thenReturn(page);

        Page<Review> result = reviewService.search(null, null, "user", pageable);

        assertEquals(1, result.getContent().size());
        verify(reviewRepository).findByUserUsernameContainingIgnoreCase("user", pageable);
    }

    @Test
    void search_withNoParams_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.search(null, null, null, pageable));
    }

    @Test
    void searchOptional_withComment_shouldReturnPage() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByCommentContainingIgnoreCase(eq("post"), any(Pageable.class))).thenReturn(page);

        Page<Review> result = reviewService.searchOptional("post", null, null, 0, 5, "id", true);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchOptional_withNoParams_shouldReturnEmptyPage() {
        Page<Review> result = reviewService.searchOptional(null, null, null, 0, 5, "id", true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchByComment_shouldDelegate() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByCommentContainingIgnoreCase("q", pageable)).thenReturn(page);

        Page<Review> result = reviewService.searchByComment("q", pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchByRating_shouldDelegate() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByRating(4, pageable)).thenReturn(page);

        Page<Review> result = reviewService.searchByRating(4, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchByAuthor_shouldDelegate() {
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByUserUsernameContainingIgnoreCase("u", pageable)).thenReturn(page);

        Page<Review> result = reviewService.searchByAuthor("u", pageable);

        assertEquals(1, result.getContent().size());
    }
}
