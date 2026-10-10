package com.invicta.service.impl;

import com.invicta.config.InvictaProperties;
import com.invicta.service.EmailCodeSender;
import com.invicta.util.EmailAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailCodeSenderImpl implements EmailCodeSender {
    private static final Logger log = LoggerFactory.getLogger(EmailCodeSenderImpl.class);

    private final JavaMailSender sender;
    private final InvictaProperties properties;

    public EmailCodeSenderImpl(JavaMailSender sender, InvictaProperties properties) {
        this.sender = sender;
        this.properties = properties;
    }

    @Override
    public void send(String email, String code) {
        if (!properties.mail().enabled()) {
            log.info("Development login code for {} is {}", EmailAddressUtils.mask(email), code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mail().from());
        message.setTo(email);
        message.setSubject("【Invicta】Verification code");
        message.setText("Your verification code is: " + code
                + ". It is valid for 1 minute. If you did not request it, please ignore this email.");
        sender.send(message);
    }
}
