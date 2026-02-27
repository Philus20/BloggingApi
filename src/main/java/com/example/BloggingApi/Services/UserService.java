package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.DTOs.Requests.EditUserRequest;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Utils.PageableUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User create(CreateUserRequest req) {
        User user = User.create(req.username(), req.email(), req.password());
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User update(EditUserRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new NullException("User not found"));
        user.update(request.username(), request.email());
        return user;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NullException("User not found"));
        userRepository.delete(user);
    }

    @Cacheable(value = "users", key = "#id")
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NullException("User not found"));
    }

    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> searchByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable);
    }

    public Page<User> searchByUsername(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    public Page<User> searchByEmail(String email, Pageable pageable) {
        return userRepository.findByEmailContainingIgnoreCase(email, pageable);
    }

    public Page<User> search(String keyword, String username, String email, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.searchByKeyword(keyword, pageable);
        }
        if (username != null && !username.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        }
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmailContainingIgnoreCase(email, pageable);
        }
        throw new IllegalArgumentException("Please provide at least one search parameter: keyword, username, or email");
    }

    public Page<User> getAll(int page, int size, String sortBy, boolean ascending) {
        return getAll(PageableUtils.create(page, size, sortBy, ascending));
    }

    public Page<User> search(String keyword, String username, String email, int page, int size, String sortBy, boolean ascending) {
        return search(keyword, username, email, PageableUtils.create(page, size, sortBy, ascending));
    }

    /** Search with optional criteria; returns empty page when no criteria provided. */
    public Page<User> searchOptional(String keyword, String username, String email, int page, int size, String sortBy, boolean ascending) {
        Pageable pageable = PageableUtils.create(page, size, sortBy, ascending);
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.searchByKeyword(keyword, pageable);
        }
        if (username != null && !username.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        }
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmailContainingIgnoreCase(email, pageable);
        }
        return Page.empty(pageable);
    }
}
