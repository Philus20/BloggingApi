package com.example.BloggingApi.Domain.Entities;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // MANY posts → ONE user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User author;

    // ONE post → MANY comments
    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Comment> comments = new ArrayList<>();

    // MANY posts ↔ MANY tags
    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
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
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getAuthor() { return author; }

}
