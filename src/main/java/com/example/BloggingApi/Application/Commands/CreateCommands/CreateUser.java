package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateUserRequest;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.UserRepository;
import org.springframework.stereotype.Service;



@Service
public class CreateUser {

    private final UserRepository userRepository;

    public CreateUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    public User handle(CreateUserRequest req) throws NullException {

        //  Validation

        if (req.email() == null || req.email().isBlank()) {
            throw new NullException("Email cannot be blank");
        }

        if (req.username() == null || req.username().isBlank()) {
            throw new NullException("Username cannot be blank");
        }

        if (req.password() == null) {
            throw new NullException("Password cannot be blank");
        }



        //  Fetch User entity

        User existingUser = userRepository.findByString(req.username());

        if (existingUser != null) {
            throw new NullException("Username already exists");
        }

        User user = User.create(req.username(), req.email(), req.password());

        userRepository.create(user);

        return user;
    }
}
