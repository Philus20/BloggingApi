package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistsUserValidator implements ConstraintValidator<ExistsUser, Long> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext context) {
        if (id == null) return false;
        return userRepository.existsById(id);
    }
}
