package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.ReviewRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;
    @MockBean
    private ReviewRepository reviewRepository;
    @MockBean
    private PostRepository postRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    void getReviewById_shouldReturn200() throws Exception {
        User user = User.create("u", "u@e.com", "p");
        Post post = Post.create("P", "C", user);
        Review review = Review.create(5, "Great", user, post);
        when(reviewService.getById(1L)).thenReturn(review);

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Great"));
    }

    @Test
    void getAllReviews_shouldReturn200() throws Exception {
        User user = User.create("u", "u@e.com", "p");
        Post post = Post.create("P", "C", user);
        Review review = Review.create(4, "Good", user, post);
        when(reviewService.getAll(0, 5, "id", true))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].rating").value(4));
    }

    @Test
    void searchReviews_shouldReturn200WhenCommentProvided() throws Exception {
        User user = User.create("u", "u@e.com", "p");
        Post post = Post.create("P", "C", user);
        Review review = Review.create(3, "Ok", user, post);
        when(reviewService.search(any(), any(), any(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/reviews/search").param("comment", "ok"))
                .andExpect(status().isOk());
    }

    @Test
    void createReview_shouldReturn200() throws Exception {
        User user = User.create("u", "u@e.com", "p");
        Post post = Post.create("P", "C", user);
        Review review = Review.create(5, "Nice", user, post);
        when(reviewService.create(any())).thenReturn(review);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(postRepository.existsById(2L)).thenReturn(true);

        String body = "{\"rating\":5,\"comment\":\"Nice\",\"userId\":1,\"postId\":2}";
        mockMvc.perform(post("/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review created successfully"));
    }

    @Test
    void editReview_shouldReturn200() throws Exception {
        User user = User.create("u", "u@e.com", "p");
        Post post = Post.create("P", "C", user);
        Review review = Review.create(4, "Updated", user, post);
        when(reviewService.update(any())).thenReturn(review);
        when(reviewRepository.existsById(1L)).thenReturn(true);

        String body = "{\"id\":1,\"rating\":4,\"comment\":\"Updated\"}";
        mockMvc.perform(put("/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReview_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));
    }
}
