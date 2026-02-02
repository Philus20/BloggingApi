package com.example.BloggingApi.Domain.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    public Review() {}
    private Review(int rating, String comment, User user, Post post) {
        this.rating = rating;
        this.comment = comment;
        this.user = user;
        this.post = post;
    }

    public static Review create(int rating, String comment, User user, Post post) {
        return new Review(rating, comment, user, post);
    }

    public void update(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public User getUser() {
        return user;
    }

    public Post getPost() {
        return post;
    }
}

