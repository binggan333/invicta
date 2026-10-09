package com.invicta.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "invicta")
public record InvictaProperties(Auth auth, Mail mail, LegacyApi legacyApi) {
    public record Auth(String jwtSecret, Duration tokenTtl, Duration codeTtl) {}
    public record Mail(boolean enabled, String from) {}
    public record LegacyApi(String baseUrl, String token) {}
}
