package com.example.BloggingApi.DTOs.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = ExistsPostValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExistsPost {
    String message() default "Post not found";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
