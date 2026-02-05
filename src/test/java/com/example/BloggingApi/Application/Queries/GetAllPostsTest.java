package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Post;
import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.*;
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
import static org.mockito.Mockito.*;

class GetAllPostsTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private GetAllPosts getAllPosts;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldReturnPageOfPosts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(Collections.emptyList());
        when(postRepository.findAll(pageable)).thenReturn(postPage);

        // Act
        Page<Post> result = getAllPosts.handle(pageable);

        // Assert
        assertNotNull(result);
        verify(postRepository, times(1)).findAll(pageable);
    }
}