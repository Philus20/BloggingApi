package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateUserRequest;
import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.stereotype.Repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateUserTest {

    private  CreateUser createUser;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        createUser = new CreateUser(userRepository);
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

        // Act
        User result = createUser.handle(request);

        // Assert
        assertNotNull(result);
        assertEquals("john_doe", result.getUsername());
        assertEquals("john@email.com", result.getEmail());

    }

}