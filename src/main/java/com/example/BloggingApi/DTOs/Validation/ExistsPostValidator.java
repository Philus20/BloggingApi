package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.PostRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistsPostValidator implements ConstraintValidator<ExistsPost, Long> {

    @Autowired
    private PostRepository postRepository;

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext context) {
        if (id == null) return false;
        return postRepository.existsById(id);
    }
}
