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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    JWTService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ActiveTokenStore activeTokenStore;
    private final SecurityEventService securityEventService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       ActiveTokenStore activeTokenStore, SecurityEventService securityEventService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.activeTokenStore = activeTokenStore;
        this.securityEventService = securityEventService;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User create(CreateUserRequest req) {
        User user = User.create(req.username(), req.email(), passwordEncoder.encode(req.password()));
        return userRepository.save(user);
    }


    /**
     * Authenticates the user and returns a JWT plus identity for the response. On failure
     * AuthenticationManager throws BadCredentialsException (handled as 401).
     * Blocks username if too many recent failures (brute-force protection).
     */
    public LoginResponse login(LoginRequest req) {
        if (securityEventService.isBlocked(req.username())) {
            securityEventService.logLoginFailure(req.username(), "Blocked: too many failed attempts");
            throw new org.springframework.security.authentication.BadCredentialsException("Account temporarily locked. Try again later.");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.username(),
                        req.password()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("User not found after authentication");
        }
        String token = jwtService.generateToken(user);
        String jti = jwtService.extractJti(token);
        long issuedAt = System.currentTimeMillis();
        if (jti != null) {
            activeTokenStore.add(jti, username, issuedAt);
        }
        securityEventService.logLoginSuccess(username);
        String role = user.getRole();
        if (role == null || role.isBlank()) {
            role = "READER";
        } else {
            role = role.trim().toUpperCase();
            if ("GENERAL".equals(role)) {
                role = "READER";
            }
        }
        long expiresAt = (System.currentTimeMillis() / 1000) + jwtService.getExpirationSeconds();
        return new LoginResponse(token, username, List.of(role), expiresAt);
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
