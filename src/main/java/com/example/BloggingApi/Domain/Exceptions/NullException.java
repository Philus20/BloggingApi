package com.example.BloggingApi.Domain.Exceptions;

public class NullException extends RuntimeException {
    public NullException(String message) {
        super(message);
    }
}
