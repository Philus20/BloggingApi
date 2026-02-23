package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.DTOs.Requests.EditUserRequest;
import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueUserForEditValidator implements ConstraintValidator<UniqueUserForEdit, EditUserRequest> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(EditUserRequest req, ConstraintValidatorContext context) {
        if (req == null || req.id() == null) return true;
        if (req.username() != null && !req.username().isBlank()) {
            User byUsername = userRepository.findByUsername(req.username());
            if (byUsername != null && !byUsername.getId().equals(req.id())) return false;
        }
        if (req.email() != null && !req.email().isBlank()) {
            User byEmail = userRepository.findByEmailIgnoreCase(req.email()).orElse(null);
            if (byEmail != null && !byEmail.getId().equals(req.id())) return false;
        }
        return true;
    }
}
