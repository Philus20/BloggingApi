package com.example.BloggingApi.API.Validation;

import jakarta.validation.ConstraintValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NumericValidator implements ConstraintValidator<Numeric, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true; // null-check handled by @NotNull if needed
        try {
            Double.parseDouble(value); // accepts integer or decimal
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
