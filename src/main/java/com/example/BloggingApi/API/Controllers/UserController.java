package com.example.BloggingApi.API.Controllers;

import com.example.BloggingApi.API.Requests.CreateUserRequest;
import com.example.BloggingApi.API.Requests.EditUserRequest;
import com.example.BloggingApi.API.Resposes.ApiResponse;
import com.example.BloggingApi.API.Resposes.UserResponse;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateUser;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteUser;
import com.example.BloggingApi.Application.Commands.EditCommands.EditUser;
import com.example.BloggingApi.Application.Queries.GetAllUsers;
import com.example.BloggingApi.Application.Queries.GetUserById;
import com.example.BloggingApi.Application.Queries.SearchUsers;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final CreateUser createUserHandler;
    private final EditUser editUserHandler;
    private final DeleteUser deleteUserHandler;
    private final GetUserById getUserByIdHandler;
    private final GetAllUsers getAllUsersHandler;
    private final SearchUsers searchUsersHandler;

    public UserController(UserRepository userRepository, CreateUser createUserHandler, EditUser editUserHandler, DeleteUser deleteUserHandler, GetUserById getUserByIdHandler, GetAllUsers getAllUsersHandler, SearchUsers searchUsersHandler) {
        this.userRepository = userRepository;
        this.createUserHandler = createUserHandler;
        this.editUserHandler = editUserHandler;
        this.deleteUserHandler = deleteUserHandler;
        this.getUserByIdHandler = getUserByIdHandler;
        this.getAllUsersHandler = getAllUsersHandler;
        this.searchUsersHandler = searchUsersHandler;
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        User user = getUserByIdHandler.handle(id);
        return ApiResponse.success("User retrieved successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @GetMapping("/users")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> usersPage = getAllUsersHandler.handle(pageable);
        Page<UserResponse> response = usersPage.map(user ->
                new UserResponse(user.getId(), user.getUsername(), user.getEmail())
        );
        return ApiResponse.success("Users retrieved successfully", response);
    }

    @GetMapping("/users/search")
    public ApiResponse<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> usersPage;

        if (keyword != null && !keyword.isBlank()) {
            usersPage = searchUsersHandler.searchByKeyword(keyword, pageable);
        } else if (username != null && !username.isBlank()) {
            usersPage = searchUsersHandler.searchByUsername(username, pageable);
        } else if (email != null && !email.isBlank()) {
            usersPage = searchUsersHandler.searchByEmail(email, pageable);
        } else {
            throw new IllegalArgumentException("Please provide at least one search parameter: keyword, username, or email");
        }

        Page<UserResponse> response = usersPage.map(user ->
                new UserResponse(user.getId(), user.getUsername(), user.getEmail())
        );
        return ApiResponse.success("Users search completed successfully", response);
    }

    @PostMapping("/users")
    public ApiResponse<UserResponse> createUser(@RequestBody @jakarta.validation.Valid CreateUserRequest request) {
        User user = createUserHandler.handle(request);
        return ApiResponse.success("User created successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @PutMapping("/users")
    public ApiResponse<UserResponse> editUser(@RequestBody @jakarta.validation.Valid EditUserRequest request) {
        User user = editUserHandler.handle(request);
        return ApiResponse.success("User updated successfully", new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        deleteUserHandler.handle(id);
        return ApiResponse.success("User deleted successfully");
    }

}

