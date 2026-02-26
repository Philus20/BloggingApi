package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.PostService;
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

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PostService postService;
    @MockBean
    private PostRepository postRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    void getPostById_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("Title", "Content", author);
        when(postService.getById(1L)).thenReturn(post);
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    void getAllPosts_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("T", "C", author);
        when(postService.getAll(0, 5, "id", true))
                .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 5), 1));
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("T"));
    }

    @Test
    void searchPosts_shouldReturn200WhenKeywordProvided() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("T", "C", author);
        when(postService.search(any(), any(), any(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 5), 1));
        mockMvc.perform(get("/posts/search").param("keyword", "q"))
                .andExpect(status().isOk());
    }

    @Test
    void createPost_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("New", "Body", author);
        when(postService.create(any())).thenReturn(post);
        when(userRepository.existsById(1L)).thenReturn(true);
        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New\",\"content\":\"Body\",\"authorId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void editPost_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("Updated", "Updated content", author);
        when(postService.update(any())).thenReturn(post);
        when(postRepository.existsById(1L)).thenReturn(true);
        mockMvc.perform(put("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"Updated\",\"content\":\"Updated content\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deletePost_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post deleted"));
    }
}
