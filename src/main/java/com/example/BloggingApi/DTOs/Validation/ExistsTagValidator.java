package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.TagRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistsTagValidator implements ConstraintValidator<ExistsTag, Long> {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext context) {
        if (id == null) return false;
        return tagRepository.existsById(id);
    }
}
