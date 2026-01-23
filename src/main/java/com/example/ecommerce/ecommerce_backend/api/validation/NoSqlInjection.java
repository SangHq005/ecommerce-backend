package com.example.ecommerce.ecommerce_backend.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to prevent SQL injection in string inputs
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NoSqlInjectionValidator.class)
public @interface NoSqlInjection {
    String message() default "Input contains potentially dangerous characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}