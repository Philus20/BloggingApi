package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

    @Test
    void getUserById_shouldReturn200AndUser() throws Exception {
        User user = User.create("john", "john@example.com", "pass");
        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.username").value("john"));
    }

    @Test
    void getAllUsers_shouldReturn200AndPage() throws Exception {
        User user = User.create("u", "u@e.com", "p");
        when(userService.getAll(0, 5, "id", true))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/users").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("u"));
    }

    @Test
    void searchUsers_shouldReturn200WhenKeywordProvided() throws Exception {
        User user = User.create("u", "u@e.com", "p");
        when(userService.search(eq("key"), any(), any(), eq(0), eq(5), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/users/search").param("keyword", "key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void createUser_shouldReturn200() throws Exception {
        User user = User.create("new", "new@example.com", "pass");
        when(userService.create(any())).thenReturn(user);
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRepository.findByEmailContainingIgnoreCase(anyString(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        String body = "{\"username\":\"new\",\"email\":\"new@example.com\",\"password\":\"Passw0rd!\"}";
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    @Test
    void editUser_shouldReturn200() throws Exception {
        User user = User.create("updated", "up@example.com", "p");
        when(userService.update(any())).thenReturn(user);
        when(userRepository.existsById(1L)).thenReturn(true);

        String body = "{\"id\":1,\"username\":\"updated\",\"email\":\"up@example.com\"}";
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }
}
