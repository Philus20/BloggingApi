package com.example.BloggingApi.Exceptions;

import com.example.BloggingApi.ResposesDTO.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {
    // Handle your custom NullException
    @ExceptionHandler(NullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) //
    public ApiResponse<String> handleNullException(NullException ex) {
        return ApiResponse.failure(ex.getMessage());
    }

    @ExceptionHandler(DuplicateEntityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<String> handleDuplicateEntityException(DuplicateEntityException ex) {
        return ApiResponse.failure(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ApiResponse.failure(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ApiResponse.failure(ex.getMessage());


    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleValidationException(ValidationException ex) {
        return ApiResponse.failure(ex.getMessage());
    }

    /** Bean Validation (e.g. @NotBlank, @Email) failures on request DTOs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ApiResponse.failure(errors.isEmpty() ? "Validation failed" : errors);
    }

    /** Invalid arguments (e.g. search params) from service layer. */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResponse.failure(ex.getMessage());
    }
}