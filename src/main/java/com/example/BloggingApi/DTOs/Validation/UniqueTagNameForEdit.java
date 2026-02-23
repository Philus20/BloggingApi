package com.example.BloggingApi.DTOs.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueTagNameForEditValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueTagNameForEdit {
    String message() default "Tag name already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
