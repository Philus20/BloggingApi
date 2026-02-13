package com.example.BloggingApi.Services;

import com.example.BloggingApi.Entities.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.RequestsDTO.CreateUserRequest;
import com.example.BloggingApi.RequestsDTO.EditUserRequest;
import com.example.BloggingApi.ResposesDTO.UserResponse;
import com.example.BloggingApi.Validation.ValidateSearchParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidateSearchParams validateSearchParams;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create("john", "john@example.com", "pwd");
    }

    @Test
    void createUser_throwsWhenUsernameExists() {
        CreateUserRequest request = new CreateUserRequest("john", "john@example.com", "pwd");
        when(userRepository.findByString("john")).thenReturn(user);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_createsAndReturnsUser() {
        CreateUserRequest request = new CreateUserRequest("john", "john@example.com", "pwd");
        when(userRepository.findByString("john")).thenReturn(null);

        User result = userService.createUser(request);

        assertThat(result.getUsername()).isEqualTo("john");
        verify(userRepository).create(any(User.class));
    }

    @Test
    void getAllUsers_returnsPageOfUserResponses() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(any())).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(0, 5, "id", true);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any());
    }

    @Test
    void getUserById_returnsUser_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_throwsNullException_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void editUser_updatesUser_whenFound() {
        EditUserRequest request = new EditUserRequest(1L, "updated", "updated@example.com");
        when(userRepository.findByInteger(1)).thenReturn(user);

        User result = userService.editUser(request);

        assertThat(result.getUsername()).isEqualTo("updated");
        verify(userRepository).findByInteger(1);
        verify(userRepository).update(user);
    }

    @Test
    void editUser_throwsWhenUserMissing() {
        EditUserRequest request = new EditUserRequest(1L, "updated", "updated@example.com");
        when(userRepository.findByInteger(1)).thenReturn(null);

        assertThatThrownBy(() -> userService.editUser(request))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_deletesWhenFound() {
        when(userRepository.findByInteger(1)).thenReturn(user);

        userService.deleteUser(1L);

        verify(userRepository).findByInteger(1);
        verify(userRepository).delete(1);
    }

    @Test
    void deleteUser_throwsWhenMissing() {
        when(userRepository.findByInteger(1)).thenReturn(null);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(NullException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void searchUsers_usesKeywordSearchWhenProvided() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.searchByKeyword(eq("spring"), any())).thenReturn(page);
        when(validateSearchParams.hasText("spring")).thenReturn(true);

        Page<UserResponse> result = userService.searchUsers("spring", null, null, 0, 5, "id", true);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(validateSearchParams).Validate("spring", null, null);
        verify(userRepository).searchByKeyword(eq("spring"), any());
    }
}
