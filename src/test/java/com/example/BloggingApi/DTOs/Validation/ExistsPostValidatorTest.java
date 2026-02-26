package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.PostRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExistsPostValidatorTest {

    private ExistsPostValidator validator;
    private PostRepository postRepository;

    @BeforeEach
    void setUp() throws Exception {
        validator = new ExistsPostValidator();
        postRepository = mock(PostRepository.class);
        Field f = ExistsPostValidator.class.getDeclaredField("postRepository");
        f.setAccessible(true);
        f.set(validator, postRepository);
        validator.initialize(mock(ExistsPost.class));
    }

    @Test
    void isValid_shouldReturnFalseWhenIdIsNull() {
        assertFalse(validator.isValid(null, mock(ConstraintValidatorContext.class)));
    }

    @Test
    void isValid_shouldReturnTrueWhenPostExists() {
        when(postRepository.existsById(1L)).thenReturn(true);
        assertTrue(validator.isValid(1L, mock(ConstraintValidatorContext.class)));
    }

    @Test
    void isValid_shouldReturnFalseWhenPostDoesNotExist() {
        when(postRepository.existsById(999L)).thenReturn(false);
        assertFalse(validator.isValid(999L, mock(ConstraintValidatorContext.class)));
    }
}
