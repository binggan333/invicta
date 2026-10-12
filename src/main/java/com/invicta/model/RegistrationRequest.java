package com.invicta.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.invalid}") String email,
        @NotBlank(message = "{validation.code.required}")
        @Pattern(regexp = "\\d{6}", message = "{validation.code.invalid}") String code,
        @NotBlank(message = "{validation.password.required}")
        @Size(min = 8, max = 72, message = "{validation.password.size}") String password
) {}
