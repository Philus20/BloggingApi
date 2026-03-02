package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.UserService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceGetAllSearchTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Mock
    private com.example.BloggingApi.Security.JWTService jwtService;

    @Mock
    private com.example.BloggingApi.Security.ActiveTokenStore activeTokenStore;

    @Mock
    private com.example.BloggingApi.Security.SecurityEventService securityEventService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll_withPageable_returnsPage() {
        Page<User> page = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.getAll(PageRequest.of(0, 5));

        assertNotNull(result);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAll_withPageSize_callsFindAll() {
        Page<User> page = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.getAll(0, 5, "id", true);

        assertNotNull(result);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void search_withKeyword_callsSearchByKeyword() {
        Page<User> page = new PageImpl<>(Collections.emptyList());
        when(userRepository.searchByKeyword(eq("test"), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.search("test", null, null, PageRequest.of(0, 5));

        assertNotNull(result);
        verify(userRepository).searchByKeyword(eq("test"), any(Pageable.class));
    }

    @Test
    void search_withUsername_callsFindByUsername() {
        Page<User> page = new PageImpl<>(Collections.emptyList());
        when(userRepository.findByUsernameContainingIgnoreCase(eq("john"), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.search(null, "john", null, PageRequest.of(0, 5));

        assertNotNull(result);
        verify(userRepository).findByUsernameContainingIgnoreCase(eq("john"), any(Pageable.class));
    }

    @Test
    void search_withEmail_callsFindByEmail() {
        Page<User> page = new PageImpl<>(Collections.emptyList());
        when(userRepository.findByEmailContainingIgnoreCase(eq("@test.com"), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.search(null, null, "@test.com", PageRequest.of(0, 5));

        assertNotNull(result);
        verify(userRepository).findByEmailContainingIgnoreCase(eq("@test.com"), any(Pageable.class));
    }

    @Test
    void search_withNoParam_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.search(null, null, null, PageRequest.of(0, 5)));
    }
}
