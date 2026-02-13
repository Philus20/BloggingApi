package com.example.BloggingApi.Controllers.RestControllers;

import com.example.BloggingApi.RequestsDTO.CreateUserRequest;
import com.example.BloggingApi.RequestsDTO.EditUserRequest;
import com.example.BloggingApi.ResposesDTO.ApiResponse;
import com.example.BloggingApi.ResposesDTO.UserResponse;
import com.example.BloggingApi.Services.UserService;
import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@Tag(name = "Users", description = "Create, read, update, and delete users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }


    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{user_id}")
    public ApiResponse<UserResponse> getUserById(@Parameter(description = "User ID") @PathVariable Long user_id) {
        User user = userService.getUserById(user_id);
        return ApiResponse.success("User retrieved successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @Operation(summary = "List all users", description = "Paginated and sorted list of users")
    @GetMapping("/users")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "user_id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy, ascending);
        return ApiResponse.success("Users retrieved successfully", users);
    }


    @Operation(summary = "Search users", description = "Search by keyword, username, or email. At least one parameter required.")
    @GetMapping("/users/search")
    public ApiResponse<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    )

    {
        Page<UserResponse> result = userService.searchUsers(
                keyword, username, email, page, size, sortBy, ascending
        );

        return ApiResponse.success("Users search completed successfully", result);
    }


    @Operation(summary = "Create user", description = "Requires username, email, and password")
    @PostMapping("/users")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(request);
        return ApiResponse.success("User created successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @Operation(summary = "Update user")
    @PutMapping("/users")
    public ApiResponse<UserResponse> editUser(@RequestBody @Valid EditUserRequest request) {
        User user = userService.editUser(request);
        return ApiResponse.success("User updated successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("User deleted successfully");
    }

}

