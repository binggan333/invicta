package com.invicta.service.impl;

import com.invicta.client.LegacyApiClient;
import com.invicta.common.TooManyCodeRequestsException;
import com.invicta.common.EmailDeliveryException;
import com.invicta.common.VerificationCodeInvalidException;
import com.invicta.common.InvalidCredentialsException;
import com.invicta.common.ExistingAccountRequiredException;
import com.invicta.config.InvictaProperties;
import com.invicta.model.EmailCodePurpose;
import com.invicta.model.OrganizationAccount;
import com.invicta.model.RegistrationRequest;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import com.invicta.service.EmailCodeSender;
import com.invicta.service.EmailCodeStore;
import com.invicta.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock SysOrganMapper organizations;
    @Mock EmailCodeStore codes;
    @Mock EmailCodeSender sender;
    @Mock TokenService tokens;
    @Mock LegacyApiClient legacy;

    private AuthServiceImpl service;
    private OrganizationAccount account;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        InvictaProperties properties = new InvictaProperties(
                new InvictaProperties.Auth("a".repeat(32), Duration.ofHours(8),
                        Duration.ofMinutes(1), Duration.ofMinutes(1)),
                new InvictaProperties.Mail(false, "no-reply@invicta.local"),
                new InvictaProperties.LegacyApi("http://localhost:8080", "", "test-key", "test-secret"));
        passwordEncoder = new BCryptPasswordEncoder(4);
        service = new AuthServiceImpl(organizations, codes, sender, tokens, properties, legacy, passwordEncoder);
        account = new OrganizationAccount(10L, "Example Org", "owner@example.com", 3, true,
                Set.of("proMerchant"));
    }

    @Test
    void rejectsRepeatedCodeRequestDuringCooldown() {
        when(organizations.selectList(any())).thenReturn(List.of(activeOrganization()));
        when(codes.issue(eq("owner@example.com"), eq(EmailCodePurpose.LOGIN), anyString(),
                eq(Duration.ofMinutes(1)), eq(Duration.ofMinutes(1)))).thenReturn(false);

        assertThatThrownBy(() -> service.requestCode("OWNER@example.com", EmailCodePurpose.LOGIN))
                .isInstanceOf(TooManyCodeRequestsException.class)
                .hasMessage("请求频繁，请稍后再试");
        verify(sender, never()).send(anyString(), anyString());
    }

    @Test
    void expiredOrConsumedCodeCannotLogin() {
        when(organizations.selectList(any())).thenReturn(List.of(activeOrganization()));
        when(codes.consume("owner@example.com", EmailCodePurpose.LOGIN, "123456")).thenReturn(false);

        assertThatThrownBy(() -> service.verify("owner@example.com", "123456"))
                .isInstanceOf(VerificationCodeInvalidException.class)
                .hasMessage("验证码已失效");
        verify(tokens, never()).issue(account);
    }

    @Test
    void releasesCodeAndCooldownWhenEmailDeliveryFails() {
        when(organizations.selectList(any())).thenReturn(List.of(activeOrganization()));
        when(codes.issue(eq("owner@example.com"), eq(EmailCodePurpose.LOGIN), anyString(),
                eq(Duration.ofMinutes(1)), eq(Duration.ofMinutes(1)))).thenReturn(true);
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(sender).send(eq("owner@example.com"), anyString());

        assertThatThrownBy(() -> service.requestCode("owner@example.com", EmailCodePurpose.LOGIN))
                .isInstanceOf(EmailDeliveryException.class);
        verify(codes).invalidate("owner@example.com", EmailCodePurpose.LOGIN);
    }

    @Test
    void logsInWithNormalizedEmailAndPassword() {
        SysOrganEntity organization = activeOrganization();
        organization.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        when(organizations.selectList(any())).thenReturn(List.of(organization));

        service.loginWithPassword(" OWNER@example.com ", "StrongPass123");

        verify(tokens).issue(account);
    }

    @Test
    void rejectsWrongPasswordWithoutIssuingToken() {
        SysOrganEntity organization = activeOrganization();
        organization.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        when(organizations.selectList(any())).thenReturn(List.of(organization));

        assertThatThrownBy(() -> service.loginWithPassword("owner@example.com", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokens, never()).issue(any());
    }

    @Test
    void registrationUsesSignedExternalApiAndReturnsOnboardingToken() {
        SysOrganEntity organization = activeOrganization();
        organization.setOrgName("owner@example.com");
        organization.setOrgType(5);
        organization.setOrgState(1);
        organization.setProfileCompleted(false);
        when(codes.consume("owner@example.com", EmailCodePurpose.REGISTRATION, "123456")).thenReturn(true);
        when(organizations.selectList(any())).thenReturn(List.of(organization));

        service.register(new RegistrationRequest("OWNER@example.com", "123456", "StrongPass123"));

        verify(legacy).postSigned(eq("/api/invicta/account/register"), any(), eq(Object.class));
        verify(tokens).issue(new OrganizationAccount(10L, "owner@example.com", "owner@example.com", 5,
                false, Set.of("onboarding")));
    }

    @Test
    void existingAccountCodeIsIsolatedByOrganizationId() {
        SysOrganEntity organization = existingOrganization();
        when(organizations.selectList(any())).thenReturn(List.of(organization));
        when(codes.issue(eq("existing:10:shared@example.com"), eq(EmailCodePurpose.LOGIN), anyString(),
                eq(Duration.ofMinutes(1)), eq(Duration.ofMinutes(1)))).thenReturn(true);

        service.requestExistingAccountCode(" Example Org ");

        verify(sender).send(eq("shared@example.com"), anyString());
    }

    @Test
    void existingLegacyEmailCannotBeRegisteredAsANewAccount() {
        when(organizations.selectCount(any())).thenReturn(0L, 1L);

        assertThatThrownBy(() -> service.requestCode("shared@example.com", EmailCodePurpose.REGISTRATION))
                .isInstanceOf(ExistingAccountRequiredException.class);
        verify(sender, never()).send(anyString(), anyString());
    }

    private SysOrganEntity activeOrganization() {
        SysOrganEntity organization = new SysOrganEntity();
        organization.setOrgId(account.orgId());
        organization.setOrgName(account.name());
        organization.setEmail(account.email());
        organization.setOrgType(account.orgType());
        organization.setProfileCompleted(true);
        organization.setOrgState(0);
        organization.setIsDelete(false);
        return organization;
    }

    private SysOrganEntity existingOrganization() {
        SysOrganEntity organization = new SysOrganEntity();
        organization.setOrgId(10L);
        organization.setOrgName("Example Org");
        organization.setOrgEmail("SHARED@example.com");
        organization.setEmail(null);
        organization.setOrgType(3);
        organization.setOrgState(0);
        organization.setIsDelete(false);
        return organization;
    }
}
