package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.DTOs.Requests.CreatePostRequest;
import com.example.BloggingApi.DTOs.Requests.EditPostRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PostRepository postRepository;

    @Test
    @WithMockUser(roles = "READER")
    void getPostById_shouldReturnPost_whenExists() throws Exception {
        User author = User.create("author1", "author@test.com", "pass");
        Post post = Post.create("Title", "Content", author);
        when(postService.getById(1L)).thenReturn(post);

        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.title").value("Title"))
                .andExpect(jsonPath("$.data.content").value("Content"));

        verify(postService).getById(1L);
    }

    @Test
    @WithMockUser(roles = "READER")
    void getPostById_shouldReturn404_whenNotFound() throws Exception {
        when(postService.getById(999L)).thenThrow(new NullException("Post not found"));

        mockMvc.perform(get("/api/v1/posts/999"))
                .andExpect(status().isNotFound());

        verify(postService).getById(999L);
    }

    @Test
    @WithMockUser(roles = "READER")
    void getAllPosts_shouldReturnPaginatedPosts() throws Exception {
        User author = User.create("author1", "author@test.com", "pass");
        Post post = Post.create("Title", "Content", author);
        Page<Post> page = new PageImpl<>(List.of(post), PageRequest.of(0, 5), 1);
        when(postService.getAll(0, 5, "id", true)).thenReturn(page);

        mockMvc.perform(get("/api/v1/posts")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(postService).getAll(0, 5, "id", true);
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createPost_shouldReturn201_whenValid() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        CreatePostRequest request = new CreatePostRequest("New Post", "Content here", 1L);
        User author = User.create("author1", "author@test.com", "pass");
        Post created = Post.create("New Post", "Content here", author);
        when(postService.create(any(CreatePostRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.title").value("New Post"));

        verify(postService).create(any(CreatePostRequest.class));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createPost_shouldReturn400_whenValidationFails() throws Exception {
        CreatePostRequest request = new CreatePostRequest("", "", null);

        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(postService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void editPost_shouldReturn200_whenValid() throws Exception {
        when(postRepository.existsById(1L)).thenReturn(true);
        EditPostRequest request = new EditPostRequest(1L, "Updated Title", "Updated content");
        User author = User.create("author1", "author@test.com", "pass");
        Post updated = Post.create("Updated Title", "Updated content", author);
        when(postService.update(any(EditPostRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(postService).update(any(EditPostRequest.class));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void deletePost_shouldReturn200_whenExists() throws Exception {
        doNothing().when(postService).delete(1L);

        mockMvc.perform(delete("/api/v1/posts/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Post deleted"));

        verify(postService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "READER")
    void searchPosts_shouldReturnResults_whenKeywordProvided() throws Exception {
        User author = User.create("author1", "author@test.com", "pass");
        Post post = Post.create("Title", "Content", author);
        Page<Post> page = new PageImpl<>(List.of(post), PageRequest.of(0, 5), 1);
        when(postService.search(eq("java"), isNull(), isNull(), eq(0), eq(5), eq("createdAt"), eq(false)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/posts/search")
                        .param("keyword", "java")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(postService).search(eq("java"), isNull(), isNull(), eq(0), eq(5), eq("createdAt"), eq(false));
    }

    @Test
    void getPostById_shouldReturn403_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isForbidden());
    }
}
