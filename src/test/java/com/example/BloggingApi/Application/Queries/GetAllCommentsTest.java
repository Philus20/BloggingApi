package com.example.BloggingApi.Application.Queries;

import com.example.BloggingApi.Domain.Entities.Comment;
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

class GetAllCommentsTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private GetAllComments getAllComments;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldReturnPageOfComments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = new PageImpl<>(Collections.emptyList());
        when(commentRepository.findAll(pageable)).thenReturn(commentPage);

        // Act
        Page<Comment> result = getAllComments.handle(pageable);

        // Assert
        assertNotNull(result);
        verify(commentRepository, times(1)).findAll(pageable);
    }
}