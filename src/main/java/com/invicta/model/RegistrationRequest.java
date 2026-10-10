package com.invicta.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public record RegistrationRequest(
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.invalid}") String email,
        @NotBlank(message = "{validation.code.required}")
        @Pattern(regexp = "\\d{6}", message = "{validation.code.invalid}") String code,
        @NotNull(message = "{validation.registration-type.required}") Type type,
        @NotBlank(message = "{validation.organization-name.required}") String organizationName,
        @NotBlank(message = "{validation.registration-code.required}") String registrationCode,
        @NotNull(message = "{validation.chain-id.required}")
        @Positive(message = "{validation.chain-id.positive}") Integer chainId,
        Boolean stakeAccount,
        Map<String, Object> details
) {
    public enum Type {
        AGENT("/sys/agent/reg"),
        MERCHANT("/sys/mer/reg"),
        INDIVIDUAL("/sys/individual/reg"),
        STANDARD("/sys/standard/reg");

        private final String legacyPath;

        Type(String legacyPath) {
            this.legacyPath = legacyPath;
        }

        public String legacyPath() {
            return legacyPath;
        }
    }
}
