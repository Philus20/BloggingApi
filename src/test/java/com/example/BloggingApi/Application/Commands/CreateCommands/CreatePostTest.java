package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreatePostRequest;
import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Domain.Entities.User;

import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.PostRepository;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreatePostTest {

    private CreatePost createPost;
    private UserRepository userRepository;
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository = Mockito.mock(PostRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        createPost = new CreatePost(postRepository, userRepository);
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

        // Act
        Post post = createPost.handle(request);

        // Assert
        assertNotNull(post);
        assertEquals("My First Post", post.getTitle());
        assertEquals("This is the content of my first post.", post.getContent());

        Mockito.verify(postRepository).create(post);
    }
}
