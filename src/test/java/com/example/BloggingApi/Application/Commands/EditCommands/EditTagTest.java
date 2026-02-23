package com.example.BloggingApi.Application.Commands.EditCommands;

import com.example.BloggingApi.Services.TagService;
import com.example.BloggingApi.DTOs.Requests.EditTagRequest;
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
import static org.mockito.Mockito.*;

class EditTagTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldUpdateTag_WhenTagExists() throws NullException {
        // Arrange
        EditTagRequest request = new EditTagRequest(1L, "UpdatedName");
        Tag tag = mock(Tag.class);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        // Act
        Tag result = tagService.update(request);

        // Assert
        assertNotNull(result);
        verify(tag).update("UpdatedName");
    }

    @Test
    void handle_ShouldThrowException_WhenTagNotFound() {
        // Arrange
        EditTagRequest request = new EditTagRequest(1L, "UpdatedName");
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullException.class, () -> tagService.update(request));
    }
}