package com.example.BloggingApi.API.Validation;

import com.example.BloggingApi.Domain.Entities.User;
import com.example.BloggingApi.Infrastructure.Persistence.Repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true; // Let @NotBlank handle nulls
        }
        // Using the search method we added earlier which returns a Page
        return userRepository.findByEmailContainingIgnoreCase(email, Pageable.unpaged()).isEmpty();
    }
}
