package com.invicta.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizationProfileRequest(
        @NotBlank(message = "{validation.organization-name.required}")
        @Size(max = 128, message = "{validation.organization-name.size}") String orgName,
        @NotNull(message = "{validation.organization-type.required}")
        @Min(value = 3, message = "{validation.organization-type.invalid}")
        @Max(value = 5, message = "{validation.organization-type.invalid}") Integer orgType
) {}
