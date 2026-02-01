package com.example.BloggingApi.API.Controllers;

import com.example.BloggingApi.API.Requests.CreateUserRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.UserResponse;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private  UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public ApiResponse<UserResponse> createUser(@RequestBody CreateUserRequest request) {

        try{
            User user = User.create(request.username(), request.email(), request.password());
            userRepository.save(user);

            return ApiResponse.success("User created successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
        } catch (Exception e) {
            return ApiResponse.failure(e.getMessage());
        }
    }

}
