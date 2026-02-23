package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.Services.PostService;
import com.example.BloggingApi.DTOs.Requests.CreatePostRequest;
import com.example.BloggingApi.Domain.Post;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.PostRepository;
import com.example.BloggingApi.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreatePostTest {

    private PostService postService;
    private UserRepository userRepository;
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository = Mockito.mock(PostRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        postService = new PostService(postRepository, userRepository);
    }

    @Test
    void handle_shouldCreatePost_whenRequestIsValid() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest(
                "My First Post",
                "This is the content of my first post.",
                1L
        );

        User author = Mockito.mock(User.class);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(author));
        Mockito.when(postRepository.save(Mockito.any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Post post = postService.create(request);

        // Assert
        assertNotNull(post);
        assertEquals("My First Post", post.getTitle());
        assertEquals("This is the content of my first post.", post.getContent());

        Mockito.verify(postRepository).save(Mockito.any(Post.class));
    }
}
