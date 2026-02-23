package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.DTOs.Requests.EditUserRequest;
import com.example.BloggingApi.Services.UserService;
import com.example.BloggingApi.Domain.User;
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
        return userService.getById(id);
    }

    @QueryMapping
    public Page<User> listUsers(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return userService.getAll(page, size, sortBy, ascending);
    }

    @QueryMapping
    public Page<User> searchUsers(@Argument String keyword, @Argument String username, @Argument String email,
                                  @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        return userService.searchOptional(keyword, username, email, page, size, sortBy, ascending);
    }

    @MutationMapping
    public User createUser(@Argument String username, @Argument String email, @Argument String password) {
        return userService.create(new CreateUserRequest(username, email, password));
    }

    @MutationMapping
    public User editUser(@Argument Long id, @Argument String username, @Argument String email) {
        return userService.update(new EditUserRequest(id, username, email));
    }

    @MutationMapping
    public String deleteUser(@Argument Long id) {
        userService.delete(id);
        return "User with ID " + id + " deleted successfully.";
    }
}
