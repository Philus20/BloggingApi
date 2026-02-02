package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GetAllUsers {

    private final UserRepository userRepository;

    public GetAllUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> handle(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}

