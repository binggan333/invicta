package com.invicta.service.impl;

import com.invicta.client.LegacyApiClient;
import com.invicta.common.TooManyCodeRequestsException;
import com.invicta.common.EmailDeliveryException;
import com.invicta.common.VerificationCodeInvalidException;
import com.invicta.config.InvictaProperties;
import com.invicta.model.EmailCodePurpose;
import com.invicta.model.OrganizationAccount;
import com.invicta.repository.OrganizationAccountRepository;
import com.invicta.service.EmailCodeSender;
import com.invicta.service.EmailCodeStore;
import com.invicta.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock OrganizationAccountRepository accounts;
    @Mock EmailCodeStore codes;
    @Mock EmailCodeSender sender;
    @Mock TokenService tokens;
    @Mock LegacyApiClient legacy;

    private AuthServiceImpl service;
    private OrganizationAccount account;

    @BeforeEach
    void setUp() {
        InvictaProperties properties = new InvictaProperties(
                new InvictaProperties.Auth("a".repeat(32), Duration.ofHours(8),
                        Duration.ofMinutes(1), Duration.ofMinutes(1)),
                new InvictaProperties.Mail(false, "no-reply@invicta.local"),
                new InvictaProperties.LegacyApi("http://localhost:8080", ""));
        service = new AuthServiceImpl(accounts, codes, sender, tokens, properties, legacy);
        account = new OrganizationAccount(10L, "Example Org", "owner@example.com", 3, Set.of("proMerchant"));
    }

    @Test
    void rejectsRepeatedCodeRequestDuringCooldown() {
        when(accounts.findActiveByEmail("owner@example.com")).thenReturn(Optional.of(account));
        when(codes.issue(eq("owner@example.com"), eq(EmailCodePurpose.LOGIN), anyString(),
                eq(Duration.ofMinutes(1)), eq(Duration.ofMinutes(1)))).thenReturn(false);

        assertThatThrownBy(() -> service.requestCode("OWNER@example.com", EmailCodePurpose.LOGIN))
                .isInstanceOf(TooManyCodeRequestsException.class)
                .hasMessage("请求频繁，请稍后再试");
        verify(sender, never()).send(anyString(), anyString());
    }

    @Test
    void expiredOrConsumedCodeCannotLogin() {
        when(accounts.findActiveByEmail("owner@example.com")).thenReturn(Optional.of(account));
        when(codes.consume("owner@example.com", EmailCodePurpose.LOGIN, "123456")).thenReturn(false);

        assertThatThrownBy(() -> service.verify("owner@example.com", "123456"))
                .isInstanceOf(VerificationCodeInvalidException.class)
                .hasMessage("验证码已失效");
        verify(tokens, never()).issue(account);
    }

    @Test
    void releasesCodeAndCooldownWhenEmailDeliveryFails() {
        when(accounts.findActiveByEmail("owner@example.com")).thenReturn(Optional.of(account));
        when(codes.issue(eq("owner@example.com"), eq(EmailCodePurpose.LOGIN), anyString(),
                eq(Duration.ofMinutes(1)), eq(Duration.ofMinutes(1)))).thenReturn(true);
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(sender).send(eq("owner@example.com"), anyString());

        assertThatThrownBy(() -> service.requestCode("owner@example.com", EmailCodePurpose.LOGIN))
                .isInstanceOf(EmailDeliveryException.class);
        verify(codes).invalidate("owner@example.com", EmailCodePurpose.LOGIN);
    }
}
