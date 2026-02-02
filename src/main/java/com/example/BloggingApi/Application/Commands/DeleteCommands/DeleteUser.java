package com.example.BloggingApi.Application.Commands.DeleteCommands;

import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteUser {

    private final UserRepository userRepository;

    public DeleteUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void handle(Long userId) throws NullException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NullException("User not found"));

        userRepository.delete(user);
    }
}
