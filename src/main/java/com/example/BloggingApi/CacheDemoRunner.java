package com.example.BloggingApi;

import com.example.BloggingApi.Services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class CacheDemoRunner implements CommandLineRunner {

    private final UserService userService;

    public CacheDemoRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        Long userId = 8L; // use an existing user ID

        System.out.println("First call:");
        userService.getById(userId);

        System.out.println("Second call:");
        userService.getById(userId); // should NOT hit DB, nothing printed

        System.out.println("Done demo.");
    }
}