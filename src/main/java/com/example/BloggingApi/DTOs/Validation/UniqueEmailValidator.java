package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.UserRepository;
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
        return userRepository.findByEmailContainingIgnoreCase(email, Pageable.unpaged()).isEmpty();
    }
}
