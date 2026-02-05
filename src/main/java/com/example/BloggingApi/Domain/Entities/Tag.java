package com.example.BloggingApi.Domain.Entities;



import jakarta.validation.constraints.*;

import java.util.HashSet;

import java.util.Set;



public class Tag {



    private Long id;



    @NotBlank

    @Size(max = 50)

    private String name;



    private Set<Post> posts = new HashSet<>();



    public Tag() {}

    private Tag(String name) {

        this.name = name;

    }



    public static Tag create(String name) {

        return new Tag(name);

    }



    public void update(String name) {

        if (name != null && !name.isBlank()) {

            this.name = name;

        }

    }



    // Getters

    public Long getId() {

        return id;

    }



    public void setId(Long id) {

        this.id = id;

    }



    public String getName() {

        return name;

    }



    public void setName(String name) {

        this.name = name;

    }



    public Set<Post> getPosts() {

        return posts;

    }



    public void setPosts(Set<Post> posts) {

        this.posts = posts;

    }

}

