package com.invicta.service.impl;

import com.invicta.config.InvictaProperties;
import com.invicta.config.MailProperties;
import com.invicta.service.EmailCodeSender;
import com.invicta.util.EmailAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Component
public class EmailCodeSenderImpl implements EmailCodeSender {
    private static final Logger log = LoggerFactory.getLogger(EmailCodeSenderImpl.class);

    private final InvictaProperties properties;
    private final MessageSource messages;
    private final List<JavaMailSenderImpl> senders;

    public EmailCodeSenderImpl(InvictaProperties properties, MailProperties mailProperties,
                               MessageSource messages) {
        this.properties = properties;
        this.messages = messages;
        this.senders = mailProperties.configs().stream().map(this::buildSender).toList();
        if (properties.mail().enabled() && senders.isEmpty()) {
            throw new IllegalStateException("At least one mail.configs entry is required when email is enabled");
        }
    }

    @Override
    public void send(String email, String code) {
        if (!properties.mail().enabled()) {
            log.info("Development login code for {} is {}", EmailAddressUtils.mask(email), code);
            return;
        }
        JavaMailSenderImpl sender = senders.get(0);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender.getUsername());
        message.setTo(email);
        message.setSubject(messages.getMessage("mail.verification.subject",
                new Object[]{properties.mail().appName()}, LocaleContextHolder.getLocale()));
        message.setText(messages.getMessage("mail.verification.body",
                new Object[]{code}, LocaleContextHolder.getLocale()));
        sender.send(message);
    }

    private JavaMailSenderImpl buildSender(MailProperties.Account account) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(account.host());
        sender.setPort(account.port() == null ? 465 : account.port());
        sender.setUsername(account.username());
        sender.setPassword(account.password());
        sender.setDefaultEncoding(account.defaultEncoding() == null ? "UTF-8" : account.defaultEncoding());
        sender.setJavaMailProperties(account.properties() == null ? new Properties() : account.properties());
        return sender;
    }
}
