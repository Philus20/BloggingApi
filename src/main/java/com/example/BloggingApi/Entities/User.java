package com.example.BloggingApi.Entities;



import jakarta.validation.constraints.*;

import java.time.LocalDateTime;



public class User {



    private Long id;



    @NotBlank

    @Size(max = 100)

    private String username;



    @Email

    @NotBlank

    private String email;



    @NotBlank

    private String password;



    @NotBlank

    private String role;



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

    

    public void setId(Long id) {

        this.id = id;

    }

    

    public String getUsername() {

        return username;

    }

    

    public void setUsername(String username) {

        this.username = username;

    }

    

    public String getEmail() {

        return email;

    }

    

    public void setEmail(String email) {

        this.email = email;

    }

    

    public String getPassword() {

        return password;

    }

    

    public void setPassword(String password) {

        this.password = password;

    }

    

    public String getRole() {

        return role;

    }

    

    public void setRole(String role) {

        this.role = role;

    }

    

    public LocalDateTime getCreatedAt() {

        return createdAt;

    }

    

    public void setCreatedAt(LocalDateTime createdAt) {

        this.createdAt = createdAt;

    }

}

