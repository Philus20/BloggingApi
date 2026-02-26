package com.example.BloggingApi.DTOs.Responses;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.Review;
import com.example.BloggingApi.Domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewResponseTest {

    @Test
    void from_shouldMapReviewToResponse() {
        User user = User.create("user", "u@e.com", "p");
        Post post = Post.create("P", "C", user);
        Review review = Review.create(5, "Great post", user, post);
        ReviewResponse res = ReviewResponse.from(review);
        assertNotNull(res);
        assertEquals(5, res.rating());
        assertEquals("Great post", res.comment());
        assertEquals("user", res.username());
    }
}
