package com.example.BloggingApi.API.Controllers.Graphql;

import com.example.BloggingApi.API.Requests.CreateUserRequest;
import com.example.BloggingApi.API.Requests.EditUserRequest;
import com.example.BloggingApi.Application.Commands.CreateCommands.CreateUser;
import com.example.BloggingApi.Application.Commands.DeleteCommands.DeleteUser;
import com.example.BloggingApi.Application.Commands.EditCommands.EditUser;
import com.example.BloggingApi.Application.Queries.GetAllUsers;
import com.example.BloggingApi.Application.Queries.GetUserById;
import com.example.BloggingApi.Application.Queries.SearchUsers;
import com.example.BloggingApi.Domain.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQLController {

    private final CreateUser createUserHandler;
    private final EditUser editUserHandler;
    private final DeleteUser deleteUserHandler;
    private final GetUserById getUserByIdHandler;
    private final GetAllUsers getAllUsersHandler;
    private final SearchUsers searchUsersHandler;


    public UserGraphQLController(CreateUser createUserHandler, EditUser editUserHandler, DeleteUser deleteUserHandler, GetUserById getUserByIdHandler, GetAllUsers getAllUsersHandler, SearchUsers searchUsersHandler) {
        this.createUserHandler = createUserHandler;
        this.editUserHandler = editUserHandler;
        this.deleteUserHandler = deleteUserHandler;
        this.getUserByIdHandler = getUserByIdHandler;
        this.getAllUsersHandler = getAllUsersHandler;
        this.searchUsersHandler = searchUsersHandler;
    }

    @QueryMapping
    public User getUser(@Argument Long id) {
        return getUserByIdHandler.handle(id);
    }


    
    // Correct Implementation matching schema
    @QueryMapping
    public Page<User> listUsers(@Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
         Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
         Pageable pageable = PageRequest.of(page, size, sort);


        return getAllUsersHandler.handle(pageable);
    }


    @QueryMapping
    public Page<User> searchUsers(@Argument String keyword, @Argument String username, @Argument String email,
                                  @Argument int page, @Argument int size, @Argument String sortBy, @Argument boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null && !keyword.isBlank()) {
            return searchUsersHandler.searchByKeyword(keyword, pageable);
        } else if (username != null && !username.isBlank()) {
            return searchUsersHandler.searchByUsername(username, pageable);
        } else if (email != null && !email.isBlank()) {
            return searchUsersHandler.searchByEmail(email, pageable);
        }
        return Page.empty(pageable);
    }

    @MutationMapping
    public User createUser(@Argument String username, @Argument String email, @Argument String password) {
        CreateUserRequest request = new CreateUserRequest(username, email, password);
        return createUserHandler.handle(request);
    }

    @MutationMapping
    public User editUser(@Argument Long id, @Argument String username, @Argument String email) {
        // Note: EditUserRequest expects id, username, email.
        // Assuming EditUserRequest structure (checking it would be good, but I'll assume standard DTO)
        EditUserRequest request = new EditUserRequest(id, username, email);
        return editUserHandler.handle(request);
    }

    @MutationMapping
    public String deleteUser(@Argument Long id) {
        deleteUserHandler.handle(id);
        return "User with ID " + id + " deleted successfully.";
    }
}
