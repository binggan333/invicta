package com.invicta.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedOrganizationTypeValidator implements ConstraintValidator<AllowedOrganizationType, Integer> {
    @Override
    public boolean isValid(Integer orgType, ConstraintValidatorContext context) {
        return orgType == null || orgType == 1 || orgType == 3 || orgType == 4 || orgType == 5;
    }
}
