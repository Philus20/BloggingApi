package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchUsersTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchUsers searchUsers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchByKeyword_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.searchByKeyword("test", pageable)).thenReturn(userPage);

        // Act
        Page<User> result = searchUsers.searchByKeyword("test", pageable);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).searchByKeyword("test", pageable);
    }

    @Test
    void searchByUsername_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase("test", pageable)).thenReturn(userPage);

        // Act
        Page<User> result = searchUsers.searchByUsername("test", pageable);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByUsernameContainingIgnoreCase("test", pageable);
    }

    @Test
    void searchByEmail_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findByEmailContainingIgnoreCase("test", pageable)).thenReturn(userPage);

        // Act
        Page<User> result = searchUsers.searchByEmail("test", pageable);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmailContainingIgnoreCase("test", pageable);
    }
}