package com.example.BloggingApi.Validation;

import com.example.BloggingApi.Repositories.TagRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueTagNameValidator implements ConstraintValidator<UniqueTagName, String> {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.isBlank()) {
            return true; // Let @NotBlank handle nulls
        }
        return true;

    }
}
