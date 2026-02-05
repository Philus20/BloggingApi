package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchUsers {

    private final UserRepository userRepository;

    public SearchUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Search users by keyword in username or email
     * Uses database indexes for efficient searching
     */
    public Page<User> searchByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * Search users by username only
     * Uses idx_user_username index
     */
    public Page<User> searchByUsername(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    /**
     * Search users by email only
     * Uses idx_user_email index
     */
    public Page<User> searchByEmail(String email, Pageable pageable) {
        return userRepository.findByEmailContainingIgnoreCase(email, pageable);
    }
}

