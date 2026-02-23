package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.CommentRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistsCommentValidator implements ConstraintValidator<ExistsComment, Long> {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext context) {
        if (id == null) return false;
        return commentRepository.existsById(id);
    }
}
