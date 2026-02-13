package com.example.BloggingApi.Entities;



import jakarta.validation.constraints.*;

import java.time.LocalDateTime;



public class Comment {



    private Long id;





    @NotBlank

    private String content;



    private LocalDateTime createdAt = LocalDateTime.now();



    private Post post;



    private User author;

public Comment() {}

    private Comment(String content, Post post, User author) {

        this.content = content;

        this.post = post;

        this.author = author;

        this.createdAt = LocalDateTime.now();

    }



    public static Comment create(String content, Post post, User author) {

        return new Comment(content, post, author);

    }



    public void update(String content) {

        if (content != null && !content.isBlank()) {

            this.content = content;

        }

    }



    // Getters

    public Long getId() {

        return id;

    }



    public void setId(Long id) {

        this.id = id;

    }



    public String getContent() {

        return content;

    }



    public void setContent(String content) {

        this.content = content;

    }



    public LocalDateTime getCreatedAt() {

        return createdAt;

    }



    public void setCreatedAt(LocalDateTime createdAt) {

        this.createdAt = createdAt;

    }



    public Post getPost() {

        return post;

    }



    public void setPost(Post post) {

        this.post = post;

    }



    public User getAuthor() {

        return author;

    }



    public void setAuthor(User author) {

        this.author = author;

    }

}

