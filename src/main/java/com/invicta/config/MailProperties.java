package com.invicta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Properties;

/**
 * SMTP account pool. The shape intentionally matches UTC Pay's mail.configs configuration.
 */
@ConfigurationProperties(prefix = "mail")
public record MailProperties(List<Account> configs) {
    public MailProperties {
        configs = configs == null ? List.of() : List.copyOf(configs);
    }

    public record Account(
            String host,
            Integer port,
            String username,
            String password,
            String defaultEncoding,
            Properties properties
    ) {}
}
