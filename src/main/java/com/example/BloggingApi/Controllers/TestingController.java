package com.example.BloggingApi.Controllers;

import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Services.CachingServiceForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/test")
@RestController
public class TestingController {

    @Autowired
    CachingServiceForTest cachingServiceForTest;

    @GetMapping("{id}")
    public User testCaching(@PathVariable Long id) {
        return cachingServiceForTest.getUserById(id);
    }

    @GetMapping("")
    public List<User> getAllUsers() {
        return cachingServiceForTest.getAllUsers();
    }

    @PutMapping("")
    public void updateUser(
            @RequestBody User user) {
        cachingServiceForTest.editUser(user);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable Long id) {
        cachingServiceForTest.deleteUser(id.intValue());

    }

}
