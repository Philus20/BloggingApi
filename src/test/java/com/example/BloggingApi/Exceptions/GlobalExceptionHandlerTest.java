package com.example.BloggingApi.Exceptions;

import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_shouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ApiResponse<Void>> res = handler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertNotNull(res.getBody());
        assertFalse(res.getBody().status());
        assertEquals("Not found", res.getBody().message());
    }

    @Test
    void handleEntityNotFound_shouldReturn404() {
        EntityNotFoundException ex = new EntityNotFoundException("Entity missing");
        ResponseEntity<ApiResponse<Void>> res = handler.handleEntityNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertNotNull(res.getBody());
        assertFalse(res.getBody().status());
    }

    @Test
    void handleNull_shouldReturn404() {
        NullException ex = new NullException("User not found");
        ResponseEntity<ApiResponse<Void>> res = handler.handleNull(ex);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("User not found", res.getBody().message());
    }

    @Test
    void handleDuplicateEntity_shouldReturn409() {
        DuplicateEntityException ex = new DuplicateEntityException("Already exists");
        ResponseEntity<ApiResponse<Void>> res = handler.handleDuplicateEntity(ex);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Already exists", res.getBody().message());
    }

    @Test
    void handleValidation_shouldReturn400() throws Exception {
        ValidationException ex = new ValidationException("Invalid input");
        ResponseEntity<ApiResponse<Void>> res = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Invalid input", res.getBody().message());
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturn400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                java.util.List.of(
                        new FieldError("user", "email", "must be valid email"),
                        new FieldError("user", "name", "must not be blank")
                ));
        ResponseEntity<ApiResponse<Void>> res = handler.handleMethodArgumentNotValid(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().message().contains("must be valid email"));
    }

    @Test
    void handleConstraintViolation_shouldReturn400() {
        ConstraintViolation<?> v = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("email");
        when(v.getPropertyPath()).thenReturn(path);
        when(v.getMessage()).thenReturn("invalid");
        ConstraintViolationException ex = new ConstraintViolationException("msg", Set.of(v));
        ResponseEntity<ApiResponse<Void>> res = handler.handleConstraintViolation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().message().contains("invalid"));
    }

    @Test
    void handleHttpMessageNotReadable_shouldReturn400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        ResponseEntity<ApiResponse<Void>> res = handler.handleHttpMessageNotReadable(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
    }

    @Test
    void handleMethodNotAllowed_shouldReturn405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("GET");
        ResponseEntity<ApiResponse<Void>> res = handler.handleMethodNotAllowed(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, res.getStatusCode());
    }

    @Test
    void handleMissingParam_shouldReturn400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("page", "int");
        ResponseEntity<ApiResponse<Void>> res = handler.handleMissingParam(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody().message().contains("page"));
    }

    @Test
    void handleTypeMismatch_shouldReturn400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("abc");
        when(ex.getName()).thenReturn("id");
        ResponseEntity<ApiResponse<Void>> res = handler.handleTypeMismatch(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody().message().contains("id"));
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad param");
        ResponseEntity<ApiResponse<Void>> res = handler.handleIllegalArgument(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("Bad param", res.getBody().message());
    }

    @Test
    void handleDataIntegrityViolation_shouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint");
        ResponseEntity<ApiResponse<Void>> res = handler.handleDataIntegrityViolation(ex);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    }

    @Test
    void handleEmptyResult_shouldReturn404() {
        EmptyResultDataAccessException ex = new EmptyResultDataAccessException(1);
        ResponseEntity<ApiResponse<Void>> res = handler.handleEmptyResult(ex);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals("Resource not found", res.getBody().message());
    }

    @Test
    void handleGeneric_shouldReturn500() {
        Exception ex = new RuntimeException("Unexpected");
        ResponseEntity<ApiResponse<Void>> res = handler.handleGeneric(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertTrue(res.getBody().message().contains("unexpected error"));
    }
}
