package com.example.BloggingApi.Application.Commands.CreateCommands;

import com.example.BloggingApi.API.Requests.CreateTagRequest;
import com.example.BloggingApi.Domain.Entities.Tag;
import com.example.BloggingApi.Domain.Exceptions.DuplicateEntityException;
import com.example.BloggingApi.Domain.Exceptions.NullException;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.TagRepository;
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
    private CreateTag createTag;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldCreateTag_WhenRequestIsValid() throws NullException, DuplicateEntityException {
        // Arrange
        CreateTagRequest req = new CreateTagRequest("Java");
        when(tagRepository.findByName("Java")).thenReturn(Optional.empty());

        // Act
        Tag result = createTag.handle(req);

        // Assert
        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void handle_ShouldThrowException_WhenTagNameIsBlank() {
        // Arrange
        CreateTagRequest req = new CreateTagRequest("");

        // Act & Assert
        assertThrows(NullException.class, () -> createTag.handle(req));
    }

    @Test
    void handle_ShouldThrowException_WhenTagAlreadyExists() {
        // Arrange
        CreateTagRequest req = new CreateTagRequest("Java");
        when(tagRepository.findByName("Java")).thenReturn(Optional.of(mock(Tag.class)));

        // Act & Assert
        DuplicateEntityException exception = assertThrows(DuplicateEntityException.class, () -> createTag.handle(req));
        assertTrue(exception.getMessage().contains("already exists"));
    }
}