package com.example.BloggingApi.Domain.Entities;

import com.example.BloggingApi.Domain.Exceptions.NullException;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

import java.util.*;



public class Post {



    private Long id;



    @NotBlank

    @Size(max = 200)

    private String title;



    @NotBlank

    private String content;



    private LocalDateTime createdAt = LocalDateTime.now();



    // MANY posts → ONE user

    private User author;



    // ONE post → MANY comments

    private List<Comment> comments = new ArrayList<>();



    // MANY posts ↔ MANY tags

    private Set<Tag> tags = new HashSet<>();



    public Post() {}

    // This constructor allows create() to work

    private Post(String title, String content, User author) {

        this.title = title;

        this.content = content;

        this.author = author;

        this.createdAt = LocalDateTime.now();

    }



    public static Post create(String title, String content, User author) {

        return new Post(title, content, author);

    }



    public void update(String title, String content) throws NullException {

        if (title == null || title.isBlank()) {

            throw new NullException("Title cannot be blank");

        }

        if (content == null || content.isBlank()) {

            throw new NullException("Content cannot be blank");

        }



        this.title = title;

        this.content = content;



}



    public Long getId() { return id; }

    

    public void setId(Long id) { this.id = id; }

    

    public String getTitle() { return title; }

    

    public void setTitle(String title) { this.title = title; }

    

    public String getContent() { return content; }

    

    public void setContent(String content) { this.content = content; }

    

    public LocalDateTime getCreatedAt() { return createdAt; }

    

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    

    public User getAuthor() { return author; }

    

    public void setAuthor(User author) { this.author = author; }

    

    public List<Comment> getComments() { return comments; }

    

    public void setComments(List<Comment> comments) { this.comments = comments; }

    

    public Set<Tag> getTags() { return tags; }

    

    public void setTags(Set<Tag> tags) { this.tags = tags; }



}

