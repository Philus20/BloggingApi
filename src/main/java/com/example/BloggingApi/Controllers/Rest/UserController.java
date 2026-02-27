package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.DTOs.Requests.EditUserRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.UserResponse;
import com.example.BloggingApi.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Users", description = "User CRUD and search")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<UserResponse> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
        var user = userService.getById(id);
        return ApiResponse.success("User retrieved successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Paginated list with optional sorting")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameter")
    })
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Users retrieved successfully", userService.getAll(page, size, sortBy, ascending).map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail())));
    }

    @GetMapping("/users/search")
    @Operation(summary = "Search users", description = "Search by keyword, username, or email")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No search parameter provided")
    })
    public ApiResponse<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Users search completed successfully", userService.search(keyword, username, email, page, size, sortBy, ascending).map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail())));
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (blank username, invalid email, weak password)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ApiResponse<UserResponse> createUser(@RequestBody @jakarta.validation.Valid CreateUserRequest request) {
        var user = userService.create(request);
        return ApiResponse.success("User created successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @PutMapping("/users")
    @Operation(summary = "Update a user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already taken")
    })
    public ApiResponse<UserResponse> editUser(@RequestBody @jakarta.validation.Valid EditUserRequest request) {
        var user = userService.update(request);
        return ApiResponse.success("User updated successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ApiResponse<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("User deleted successfully");
    }

}
