package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.Services.UserService;
import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_shouldCreateUser_whenRequestIsValid() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "john_doe",
                "john@email.com",
                "password123"
        );

        when(userRepository.findByUsername("john_doe")).thenReturn(null);
        when(passwordEncoder.encode(any(CharSequence.class))).thenAnswer(invocation -> invocation.getArgument(0).toString());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("john_doe", result.getUsername());
        assertEquals("john@email.com", result.getEmail());

    }

}