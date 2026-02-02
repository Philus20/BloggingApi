package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.API.Requests.EditUserRequest;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EditUser {

    private final UserRepository userRepository;

    public EditUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User handle(EditUserRequest request) throws NullException {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new NullException("User not found"));

        user.update(request.username(), request.email());

        return user;
    }
}
