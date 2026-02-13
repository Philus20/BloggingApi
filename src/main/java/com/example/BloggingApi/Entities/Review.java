package com.example.BloggingApi.Entities;



import jakarta.validation.constraints.*;



public class Review {



    private Long id;



    @Min(1)

    @Max(5)

    private int rating;



    private String comment;



    private User user;



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

    public void setId(Long id) {
        this.id = id;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPost(Post post) {
        this.post = post;
    }

}



