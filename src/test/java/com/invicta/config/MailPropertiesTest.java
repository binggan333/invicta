package com.invicta.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MailPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "mail.configs[0].host=smtp.gmail.com",
                    "mail.configs[0].port=465",
                    "mail.configs[0].username=sender@example.com",
                    "mail.configs[0].password=secret",
                    "mail.configs[0].default-encoding=UTF-8",
                    "mail.configs[0].properties.mail.smtp.auth=true"
            );

    @Test
    void bindsUtcPayCompatibleMailConfiguration() {
        contextRunner.run(context -> {
            MailProperties properties = context.getBean(MailProperties.class);

            assertThat(properties.configs()).hasSize(1);
            assertThat(properties.configs().get(0).username()).isEqualTo("sender@example.com");
            assertThat(properties.configs().get(0).properties().getProperty("mail.smtp.auth"))
                    .isEqualTo("true");
        });
    }

    @EnableConfigurationProperties(MailProperties.class)
    static class TestConfiguration {}
}
