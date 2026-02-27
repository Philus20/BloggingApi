package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.DTOs.Requests.EditUserRequest;
import com.example.BloggingApi.DTOs.Requests.LoginRequest;
import com.example.BloggingApi.DTOs.Responses.LoginResponse;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Security.ActiveTokenStore;
import com.example.BloggingApi.Security.JWTService;
import com.example.BloggingApi.Security.SecurityEventService;
import com.example.BloggingApi.Utils.PageableUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final ActiveTokenStore activeTokenStore;
    private final SecurityEventService securityEventService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager,
                       JWTService jwtService, ActiveTokenStore activeTokenStore,
                       SecurityEventService securityEventService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.activeTokenStore = activeTokenStore;
        this.securityEventService = securityEventService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username());
        if (user == null) {
            throw new NullException("User not found");
        }

        String token = jwtService.generateToken(user);
        String jti = jwtService.extractJti(token);
        long expiresAt = jwtService.extractExpiration(token).getTime() / 1000;

        activeTokenStore.add(jti, user.getUsername(), System.currentTimeMillis());
        securityEventService.logLoginSuccess(user.getUsername());

        return new LoginResponse(token, user.getUsername(), List.of(user.getRole()), expiresAt);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User create(CreateUserRequest req) {
        String hashedPassword = passwordEncoder.encode(req.password());
        User user = User.create(req.username(), req.email(), hashedPassword);
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

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NullException("User not found"));
    }

    @Transactional(readOnly = true)
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Cacheable(value = "users")
    public Page<User> getAll(int page, int size, String sortBy, boolean ascending) {
        return getAll(PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users")
    public Page<User> search(String keyword, String username, String email, int page, int size, String sortBy, boolean ascending) {
        return search(keyword, username, email, PageableUtils.create(page, size, sortBy, ascending));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users")
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
