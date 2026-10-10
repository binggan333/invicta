package com.invicta.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public record RegistrationRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}") String code,
        @NotNull Type type,
        @NotBlank String organizationName,
        @NotBlank String registrationCode,
        @NotNull @Positive Integer chainId,
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
