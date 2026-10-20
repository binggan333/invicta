package com.invicta.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedOrganizationTypeValidator.class)
public @interface AllowedOrganizationType {
    String message() default "{validation.organization-type.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
