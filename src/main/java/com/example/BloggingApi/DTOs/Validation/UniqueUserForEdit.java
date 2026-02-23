package com.example.BloggingApi.DTOs.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueUserForEditValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueUserForEdit {
    String message() default "Username or email already in use";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
