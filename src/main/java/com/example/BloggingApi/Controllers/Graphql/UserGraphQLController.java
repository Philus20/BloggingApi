package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.RequestsDTO.CreateUserRequest;
import com.example.BloggingApi.RequestsDTO.EditUserRequest;
import com.example.BloggingApi.ResposesDTO.UserResponse;
import com.example.BloggingApi.Services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQLController {

    private final UserService userService;

    public UserGraphQLController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public User getUser(@Argument Long id) {
        return userService.getUserById(id);
    }

    @QueryMapping
    public Page<UserResponse> listUsers(
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return userService.getAllUsers(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<UserResponse> searchUsers(
            @Argument String keyword,
            @Argument String username,
            @Argument String email,
            @Argument int page,
            @Argument int size,
            @Argument String sortBy,
            @Argument boolean ascending
    ) {
        return userService.searchUsers(keyword, username, email, page, size, sortBy, ascending);
    }

    @MutationMapping
    public User createUser(
            @Argument String username,
            @Argument String email,
            @Argument String password
    ) {
        CreateUserRequest request = new CreateUserRequest(username, email, password);
        return userService.createUser(request);
    }

    @MutationMapping
    public User editUser(
            @Argument Long id,
            @Argument String username,
            @Argument String email
    ) {
        EditUserRequest request = new EditUserRequest(id, username, email);
        return userService.editUser(request);
    }

    @MutationMapping
    public String deleteUser(@Argument Long id) {
        userService.deleteUser(id);
        return "User with ID " + id + " deleted successfully.";
    }
}
