package com.invicta.config;

import com.invicta.service.EmailCodeSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailCodeSenderConfig {
    private static final Logger log = LoggerFactory.getLogger(EmailCodeSenderConfig.class);

    @Bean
    EmailCodeSender emailCodeSender(JavaMailSender mailSender, InvictaProperties properties) {
        if (!properties.mail().enabled()) {
            return (email, code) -> log.info("Development login code for {} is {}", mask(email), code);
        }
        return (email, code) -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.mail().from());
            message.setTo(email);
            message.setSubject("Your Invicta login code");
            message.setText("Your Invicta login code is: " + code + "\nIt expires shortly.");
            mailSender.send(message);
        };
    }

    private static String mask(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" : email.charAt(0) + "***" + email.substring(at);
    }
}
