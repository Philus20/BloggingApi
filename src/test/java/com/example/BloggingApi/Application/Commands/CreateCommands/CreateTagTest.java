package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.Services.TagService;
import com.example.BloggingApi.DTOs.Requests.CreateTagRequest;
import com.example.BloggingApi.Domain.Tag;
import com.example.BloggingApi.Exceptions.NullException;
import com.example.BloggingApi.Repositories.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateTagTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldCreateTag_WhenRequestIsValid() throws NullException {
        // Arrange
        CreateTagRequest req = new CreateTagRequest("Java");
        when(tagRepository.findByName("Java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tag result = tagService.create(req);

        // Assert
        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    // Tag name blank and duplicate checks are validated at request level via @NotBlank and @UniqueTagName
}