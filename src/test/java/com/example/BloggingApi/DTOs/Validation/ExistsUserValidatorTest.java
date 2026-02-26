package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.UserRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExistsUserValidatorTest {

    private ExistsUserValidator validator;
    private UserRepository userRepository;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() throws Exception {
        validator = new ExistsUserValidator();
        userRepository = mock(UserRepository.class);
        setRepository(validator, userRepository);
        validator.initialize(mock(ExistsUser.class));
        context = mock(ConstraintValidatorContext.class);
    }

    private static void setRepository(ExistsUserValidator v, UserRepository repo) throws Exception {
        Field f = ExistsUserValidator.class.getDeclaredField("userRepository");
        f.setAccessible(true);
        f.set(v, repo);
    }

    @Test
    void isValid_shouldReturnFalseWhenIdIsNull() {
        assertFalse(validator.isValid(null, context));
        verify(userRepository, never()).existsById(any());
    }

    @Test
    void isValid_shouldReturnTrueWhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        assertTrue(validator.isValid(1L, context));
        verify(userRepository).existsById(1L);
    }

    @Test
    void isValid_shouldReturnFalseWhenUserDoesNotExist() {
        when(userRepository.existsById(999L)).thenReturn(false);
        assertFalse(validator.isValid(999L, context));
        verify(userRepository).existsById(999L);
    }
}
