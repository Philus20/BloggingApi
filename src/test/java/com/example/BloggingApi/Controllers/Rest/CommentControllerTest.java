package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.DTOs.Requests.CreateCommentRequest;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.CommentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CommentRepository commentRepository;

    @Test
    @WithMockUser(roles = "READER")
    void getCommentById_shouldReturnComment_whenExists() throws Exception {
        User author = User.create("user1", "user@test.com", "pass");
        Post post = Post.create("Post", "Content", author);
        Comment comment = Comment.create("Comment content", post, author);
        when(commentService.getById(1L)).thenReturn(comment);

        mockMvc.perform(get("/api/v1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(commentService).getById(1L);
    }

    @Test
    @WithMockUser(roles = "READER")
    void getCommentById_shouldReturn404_whenNotFound() throws Exception {
        when(commentService.getById(999L)).thenThrow(new NullException("Comment not found"));

        mockMvc.perform(get("/api/v1/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createComment_shouldReturn200_whenValid() throws Exception {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        CreateCommentRequest request = new CreateCommentRequest("New comment", 1L, 1L);
        User author = User.create("user1", "user@test.com", "pass");
        Post post = Post.create("Post", "Content", author);
        Comment created = Comment.create("New comment", post, author);
        when(commentService.create(any(CreateCommentRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(commentService).create(any(CreateCommentRequest.class));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void createComment_shouldReturn400_whenValidationFails() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("", null, null);

        mockMvc.perform(post("/api/v1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void deleteComment_shouldReturn200_whenExists() throws Exception {
        doNothing().when(commentService).delete(1L);

        mockMvc.perform(delete("/api/v1/comments/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(commentService).delete(1L);
    }
}
