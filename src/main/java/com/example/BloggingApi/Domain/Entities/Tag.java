package com.example.BloggingApi.Domain.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
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

    public String getName() {
        return name;
    }

    public Set<Post> getPosts() {
        return posts;
    }
}
