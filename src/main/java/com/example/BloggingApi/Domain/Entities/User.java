package com.example.BloggingApi.Domain.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "user_name", nullable = false)
    private String username;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    public  User(){}
    private  User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = "General";
        this.createdAt = LocalDateTime.now();
    }

    public static User create(String username, String email, String password) {
        return new User(username, email, password);
    }

    public void update(String username, String email) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
    }

    //getters and setters
    public  Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
}
