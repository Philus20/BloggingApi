package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.DTOs.Requests.EditUserRequest;
import com.example.BloggingApi.DTOs.Requests.LoginRequest;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.LoginResponse;
import com.example.BloggingApi.DTOs.Responses.UserResponse;
import com.example.BloggingApi.Security.SecurityEventService;
import com.example.BloggingApi.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Users", description = "User CRUD and search")
public class UserController {

    private final UserService userService;
    private final SecurityEventService securityEventService;

    public UserController(UserService userService, SecurityEventService securityEventService) {
        this.userService = userService;
        this.securityEventService = securityEventService;
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
        var user = userService.getById(id);
        return ApiResponse.success("User retrieved successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    @Operation(summary = "Get all users", description = "Paginated list with optional sorting")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        return ApiResponse.success("Users retrieved successfully", userService.getAll(page, size, sortBy, ascending).map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail())));
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    @Operation(summary = "Search users", description = "Search by keyword, username, or email")
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

    @PostMapping("/register")
    @Operation(summary = "Create a new user")
    public ApiResponse<UserResponse> createUser(@RequestBody @jakarta.validation.Valid CreateUserRequest request) {
        var user = userService.create(request);
        return ApiResponse.success("User created successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }


    @PostMapping("/login")
    @Operation(summary = "Login (legacy)", description = "Same as POST /api/v1/auth/login. Returns JWT and user identity.")
    public ApiResponse<LoginResponse> loginUser(@RequestBody @jakarta.validation.Valid LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ApiResponse.success("Login successful", response);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            securityEventService.logLoginFailure(request.username(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/users")
    @PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")
    @Operation(summary = "Update a user")
    public ApiResponse<UserResponse> editUser(@RequestBody @jakarta.validation.Valid EditUserRequest request) {
        var user = userService.update(request);
        return ApiResponse.success("User updated successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user")
    public ApiResponse<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("User deleted successfully");
    }

}
