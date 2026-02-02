package com.example.BloggingApi.Domain.Exceptions;

public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
