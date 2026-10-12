package com.invicta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "invicta")
public record InvictaProperties(Auth auth, Mail mail, LegacyApi legacyApi) {
    public record Auth(String jwtSecret, Duration tokenTtl, Duration codeTtl, Duration codeCooldown) {}
    public record Mail(boolean enabled, String appName) {}
    public record LegacyApi(String baseUrl, String token, String apiKey, String secretKey) {}
}
