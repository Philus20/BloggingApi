package com.example.BloggingApi.Controllers.Rest;

import com.example.BloggingApi.Domain.Comment;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.CommentRepository;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import com.example.BloggingApi.Services.CommentService;
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

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;
    @MockBean
    private CommentRepository commentRepository;
    @MockBean
    private PostRepository postRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    void getCommentById_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("P", "C", author);
        Comment comment = Comment.create("Comment text", post, author);
        when(commentService.getById(1L)).thenReturn(comment);
        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Comment text"));
    }

    @Test
    void getAllComments_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("P", "C", author);
        Comment comment = Comment.create("c", post, author);
        when(commentService.getAll(0, 5, "id", true))
                .thenReturn(new PageImpl<>(List.of(comment), PageRequest.of(0, 5), 1));
        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].content").value("c"));
    }

    @Test
    void searchComments_shouldReturn200WhenContentProvided() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("P", "C", author);
        Comment comment = Comment.create("text", post, author);
        when(commentService.search(any(), any(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(comment), PageRequest.of(0, 5), 1));
        mockMvc.perform(get("/comments/search").param("content", "text"))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("P", "C", author);
        Comment comment = Comment.create("New", post, author);
        when(commentService.create(any())).thenReturn(comment);
        when(postRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);
        mockMvc.perform(post("/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"New\",\"postId\":1,\"authorId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment created successfully"));
    }

    @Test
    void editComment_shouldReturn200() throws Exception {
        User author = User.create("a", "a@e.com", "p");
        Post post = Post.create("P", "C", author);
        Comment comment = Comment.create("Updated", post, author);
        when(commentService.update(any())).thenReturn(comment);
        when(commentRepository.existsById(1L)).thenReturn(true);
        mockMvc.perform(put("/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"content\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));
    }
}
