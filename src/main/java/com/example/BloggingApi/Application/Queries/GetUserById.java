package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class GetUserById {

    private final UserRepository userRepository;

    public GetUserById(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handle(Long userId) throws NullException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NullException("User not found"));
    }
}
