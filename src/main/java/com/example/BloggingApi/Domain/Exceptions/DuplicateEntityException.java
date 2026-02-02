package com.example.BloggingApi.Domain.Exceptions;

public class DuplicateEntityException extends Exception {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
