package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.UserResponse;
import com.example.BloggingApi.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Registration", description = "Public user registration")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Public endpoint. Creates a user with READER role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ApiResponse<UserResponse> register(@RequestBody @jakarta.validation.Valid CreateUserRequest request) {
        var user = userService.create(request);
        return ApiResponse.success("User registered successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }
}
