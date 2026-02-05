package com.example.BloggingApi.API.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NumericValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric {
    String message() default "must be a number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
