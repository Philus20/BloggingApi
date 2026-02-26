package com.example.BloggingApi.Services;

import com.example.BloggingApi.DTOs.Requests.CreateUserRequest;
import com.example.BloggingApi.DTOs.Requests.EditUserRequest;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = User.create("testuser", "test@example.com", "password");
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_shouldSaveAndReturnUser() {
        CreateUserRequest req = new CreateUserRequest("newuser", "new@example.com", "pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.create(req);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_shouldUpdateAndReturnUser() {
        EditUserRequest req = new EditUserRequest(1L, "updated", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.update(req);

        assertNotNull(result);
        assertEquals("updated", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        EditUserRequest req = new EditUserRequest(999L, "x", "x@x.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> userService.update(req));
        verify(userRepository).findById(999L);
    }

    @Test
    void delete_shouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.delete(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void delete_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> userService.delete(999L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getById_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NullException.class, () -> userService.getById(999L));
    }

    @Test
    void getAll_withPageable_shouldReturnPage() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getAll_withParams_shouldDelegateToPageable() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.getAll(0, 5, "id", true);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void search_byKeyword_shouldCallSearchByKeyword() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.searchByKeyword("test", pageable)).thenReturn(page);

        Page<User> result = userService.search("test", null, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).searchByKeyword("test", pageable);
    }

    @Test
    void search_byUsername_shouldCallFindByUsername() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByUsernameContainingIgnoreCase("user", pageable)).thenReturn(page);

        Page<User> result = userService.search(null, "user", null, pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findByUsernameContainingIgnoreCase("user", pageable);
    }

    @Test
    void search_byEmail_shouldCallFindByEmail() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByEmailContainingIgnoreCase("test", pageable)).thenReturn(page);

        Page<User> result = userService.search(null, null, "test", pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findByEmailContainingIgnoreCase("test", pageable);
    }

    @Test
    void search_withNoParams_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.search(null, null, null, pageable));
    }

    @Test
    void searchOptional_withKeyword_shouldReturnPage() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.searchByKeyword(eq("key"), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.searchOptional("key", null, null, 0, 5, "id", true);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchOptional_withNoParams_shouldReturnEmptyPage() {
        Page<User> result = userService.searchOptional(null, null, null, 0, 5, "id", true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchByKeyword_shouldDelegate() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.searchByKeyword("key", pageable)).thenReturn(page);

        Page<User> result = userService.searchByKeyword("key", pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).searchByKeyword("key", pageable);
    }

    @Test
    void searchByUsername_shouldDelegate() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByUsernameContainingIgnoreCase("name", pageable)).thenReturn(page);

        Page<User> result = userService.searchByUsername("name", pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findByUsernameContainingIgnoreCase("name", pageable);
    }

    @Test
    void searchByEmail_shouldDelegate() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByEmailContainingIgnoreCase("mail", pageable)).thenReturn(page);

        Page<User> result = userService.searchByEmail("mail", pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findByEmailContainingIgnoreCase("mail", pageable);
    }

    @Test
    void search_withPageParams_shouldDelegate() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.searchByKeyword(eq("q"), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.search("q", null, null, 0, 10, "username", false);

        assertEquals(1, result.getContent().size());
    }
}
