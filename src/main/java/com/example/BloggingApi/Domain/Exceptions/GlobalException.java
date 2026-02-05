package com.example.BloggingApi.Domain.Exceptions;

import com.example.BloggingApi.API.Resposes.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

        return  ApiResponse.failure(ex.getMessage());

    }

}