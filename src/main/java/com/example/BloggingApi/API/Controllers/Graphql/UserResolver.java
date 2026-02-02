package com.example.BloggingApi.API.Controllers.Graphql;



import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserResolver {

    private final UserRepository userRepository;

    public UserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @QueryMapping  // Matches GraphQL query name
    public User getUser(@Argument Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<User> listUsers() {
        return userRepository.findAll();
    }


}