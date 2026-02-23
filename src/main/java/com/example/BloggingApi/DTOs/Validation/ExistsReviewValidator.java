package com.example.BloggingApi.DTOs.Validation;

import com.example.BloggingApi.Repositories.ReviewRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistsReviewValidator implements ConstraintValidator<ExistsReview, Long> {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext context) {
        if (id == null) return false;
        return reviewRepository.existsById(id);
    }
}
