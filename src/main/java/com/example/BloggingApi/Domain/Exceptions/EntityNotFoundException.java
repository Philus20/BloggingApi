package com.example.BloggingApi.Domain.Exceptions;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
