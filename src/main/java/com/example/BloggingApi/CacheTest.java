package com.example.BloggingApi;

import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CacheTest implements CommandLineRunner {

    private final UserService userService;

    public CacheTest(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        Long userId = 8L; // use an existing user ID

        System.out.println("First call:");
        userService.getById(userId); // hits DB, prints "Fetching user from DB..."

        System.out.println("Second call:");
        userService.getById(userId); // should NOT hit DB, nothing printed

        System.out.println("Done demo.");
    }
}