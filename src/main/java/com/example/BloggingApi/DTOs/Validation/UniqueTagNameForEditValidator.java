package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.DTOs.Requests.EditTagRequest;
import com.example.BloggingApi.Repositories.TagRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueTagNameForEditValidator implements ConstraintValidator<UniqueTagNameForEdit, EditTagRequest> {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public boolean isValid(EditTagRequest req, ConstraintValidatorContext context) {
        if (req == null || req.id() == null || req.name() == null || req.name().isBlank()) return true;
        return tagRepository.findByName(req.name())
                .map(tag -> tag.getId().equals(req.id()))
                .orElse(true);
    }
}
