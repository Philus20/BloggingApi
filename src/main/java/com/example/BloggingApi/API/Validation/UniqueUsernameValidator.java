package com.example.BloggingApi.API.Validation;

import com.example.BloggingApi.Infrastructure.Persistence.Database.Repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            return true; // Let @NotBlank handle nulls
        }
        return userRepository.findByString(username) == null;
    }
}
