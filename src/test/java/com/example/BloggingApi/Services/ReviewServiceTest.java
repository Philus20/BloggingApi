package com.example.BloggingApi.Services;

import com.example.BloggingApi.Entities.Post;
import com.example.BloggingApi.Entities.Review;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.ReviewRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.CreateReviewRequest;
import com.example.BloggingApi.RequestsDTO.EditReviewRequest;
import com.example.BloggingApi.ResposesDTO.ReviewResponse;
import com.example.BloggingApi.Validation.ValidateSearchParams;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ValidateSearchParams validateSearchParams;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Post post;
    private Review review;

    @BeforeEach
    void setUp() {
        user = User.create("john", "john@example.com", "pwd");
        post = Post.create("Title", "Content", user);
        review = Review.create(5, "Great", user, post);
    }

    @Test
    void createReview_createsAndReturnsReview_whenUserAndPostExist() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Great", 1L, 2L);
        when(userRepository.findByInteger(1)).thenReturn(user);
        when(postRepository.findByInteger(2)).thenReturn(post);

        Review result = reviewService.createReview(request);

        assertThat(result.getRating()).isEqualTo(5);
        verify(reviewRepository).create(any(Review.class));
    }

    @Test
    void createReview_throwsWhenUserMissing() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Great", 1L, 2L);
        when(userRepository.findByInteger(1)).thenReturn(null);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createReview_throwsWhenPostMissing() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Great", 1L, 2L);
        when(userRepository.findByInteger(1)).thenReturn(user);
        when(postRepository.findByInteger(2)).thenReturn(null);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void editReview_updatesExistingReview() {
        EditReviewRequest request = new EditReviewRequest(1L, 4, "Good");
        when(reviewRepository.findByInteger(1)).thenReturn(review);

        Review result = reviewService.editReview(request);

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Good");
        verify(reviewRepository).findByInteger(1);
    }

    @Test
    void deleteReview_deletesWhenFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L);

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_throwsNullException_whenNotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    void getAllReviews_returnsPageOfResponses() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
        when(reviewRepository.findAll(any())).thenReturn(page);

        Page<ReviewResponse> result = reviewService.getAllReviews(0, 5, "id", true);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(reviewRepository).findAll(any());
    }

    @Test
    void getReviewById_returnsReview_whenFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Review result = reviewService.getReviewById(1L);

        assertThat(result).isEqualTo(review);
        verify(reviewRepository).findById(1L);
    }

    @Test
    void getReviewById_throwsNullException_whenNotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    void search_usesCommentSearchWhenProvided() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
        when(reviewRepository.findByCommentContainingIgnoreCase(eq("great"), any()))
                .thenReturn(page);
        when(validateSearchParams.hasText("great")).thenReturn(true);

        Page<ReviewResponse> result = reviewService.search("great", null, null, 0, 5, "id", true);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(validateSearchParams).validateSearchParamsForReview("great", null, null);
        verify(reviewRepository).findByCommentContainingIgnoreCase(eq("great"), any());
    }
}
