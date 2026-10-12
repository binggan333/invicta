package com.invicta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.client.LegacyApiClient;
import com.invicta.common.AccountUnavailableException;
import com.invicta.common.EmailAlreadyRegisteredException;
import com.invicta.common.EmailDeliveryException;
import com.invicta.common.InvalidCredentialsException;
import com.invicta.common.TooManyCodeRequestsException;
import com.invicta.common.VerificationCodeInvalidException;
import com.invicta.config.InvictaProperties;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import com.invicta.model.EmailCodePurpose;
import com.invicta.model.OrganizationAccount;
import com.invicta.model.OrganizationProfileRequest;
import com.invicta.model.RegistrationRequest;
import com.invicta.service.AuthService;
import com.invicta.service.EmailCodeSender;
import com.invicta.service.EmailCodeStore;
import com.invicta.service.TokenService;
import com.invicta.util.EmailAddressUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysOrganMapper organizationMapper;
    private final EmailCodeStore codes;
    private final EmailCodeSender sender;
    private final TokenService tokens;
    private final InvictaProperties properties;
    private final LegacyApiClient legacy;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public AuthServiceImpl(SysOrganMapper organizationMapper, EmailCodeStore codes, EmailCodeSender sender,
                           TokenService tokens, InvictaProperties properties, LegacyApiClient legacy,
                           PasswordEncoder passwordEncoder) {
        this.organizationMapper = organizationMapper;
        this.codes = codes;
        this.sender = sender;
        this.tokens = tokens;
        this.properties = properties;
        this.legacy = legacy;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void requestCode(String email, EmailCodePurpose purpose) {
        String normalizedEmail = EmailAddressUtils.normalize(email);
        validatePurpose(normalizedEmail, purpose);

        String code = "%06d".formatted(random.nextInt(1_000_000));
        boolean issued = codes.issue(normalizedEmail, purpose, code,
                properties.auth().codeTtl(), properties.auth().codeCooldown());
        if (!issued) {
            throw new TooManyCodeRequestsException();
        }
        try {
            sender.send(normalizedEmail, code);
        } catch (RuntimeException exception) {
            codes.invalidate(normalizedEmail, purpose);
            throw new EmailDeliveryException(exception);
        }
    }

    @Override
    public TokenService.TokenResponse verify(String email, String code) {
        String normalizedEmail = EmailAddressUtils.normalize(email);
        OrganizationAccount account = findActiveByEmail(normalizedEmail)
                .map(this::toAccount)
                .orElseThrow(AccountUnavailableException::new);
        if (!codes.consume(normalizedEmail, EmailCodePurpose.LOGIN, code)) {
            throw new VerificationCodeInvalidException();
        }
        return tokens.issue(account);
    }

    @Override
    public TokenService.TokenResponse loginWithPassword(String email, String password) {
        String normalizedEmail = EmailAddressUtils.normalize(email);
        SysOrganEntity organization = findActiveByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);
        if (organization.getPasswordHash() == null
                || !passwordEncoder.matches(password, organization.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokens.issue(toAccount(organization));
    }

    @Override
    public TokenService.TokenResponse register(RegistrationRequest request) {
        String email = EmailAddressUtils.normalize(request.email());
        validatePassword(request.password());
        if (existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        if (!codes.consume(email, EmailCodePurpose.REGISTRATION, request.code())) {
            throw new VerificationCodeInvalidException();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", request.password());
        legacy.postSigned("/api/invicta/account/register", body, Object.class);
        SysOrganEntity organization = findActiveByEmail(email).orElseThrow(AccountUnavailableException::new);
        return tokens.issue(toAccount(organization));
    }

    @Override
    public TokenService.TokenResponse completeProfile(long orgId, OrganizationProfileRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgId", orgId);
        body.put("orgName", request.orgName().trim());
        body.put("orgType", request.orgType());
        legacy.postSigned("/api/invicta/organization/profile", body, Object.class);

        SysOrganEntity organization = findActiveById(orgId).orElseThrow(AccountUnavailableException::new);
        return tokens.issue(toAccount(organization));
    }

    private void validatePurpose(String email, EmailCodePurpose purpose) {
        if (purpose == EmailCodePurpose.LOGIN && findActiveByEmail(email).isEmpty()) {
            throw new AccountUnavailableException();
        }
        if (purpose == EmailCodePurpose.REGISTRATION && existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    private Optional<SysOrganEntity> findActiveByEmail(String email) {
        LambdaQueryWrapper<SysOrganEntity> query = availableLoginAccountQuery();
        query.eq(SysOrganEntity::getEmail, email);
        List<SysOrganEntity> organizations = organizationMapper.selectList(query);
        return organizations.stream().findFirst();
    }

    private Optional<SysOrganEntity> findActiveById(long orgId) {
        LambdaQueryWrapper<SysOrganEntity> query = activeOrganizationQuery();
        query.eq(SysOrganEntity::getOrgId, orgId);
        return Optional.ofNullable(organizationMapper.selectOne(query));
    }

    private LambdaQueryWrapper<SysOrganEntity> activeOrganizationQuery() {
        LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
        query.eq(SysOrganEntity::getOrgState, 0);
        notDeleted(query);
        return query;
    }

    private LambdaQueryWrapper<SysOrganEntity> availableLoginAccountQuery() {
        LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
        query.and(item -> item.eq(SysOrganEntity::getOrgState, 0)
                .or(pending -> pending.eq(SysOrganEntity::getOrgState, 1)
                        .eq(SysOrganEntity::getProfileCompleted, false)));
        notDeleted(query);
        return query;
    }

    private void notDeleted(LambdaQueryWrapper<SysOrganEntity> query) {
        query.and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                .or().isNull(SysOrganEntity::getIsDelete));
    }

    private boolean existsByEmail(String email) {
        LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
        query.eq(SysOrganEntity::getEmail, email);
        return organizationMapper.selectCount(query) > 0;
    }

    private OrganizationAccount toAccount(SysOrganEntity organization) {
        int orgType = organization.getOrgType() == null ? -1 : organization.getOrgType();
        boolean profileCompleted = Boolean.TRUE.equals(organization.getProfileCompleted());
        String name = profileCompleted ? organization.getOrgName() : organization.getEmail();
        return new OrganizationAccount(organization.getOrgId(), name, organization.getEmail(), orgType,
                profileCompleted, Set.of(roleFor(orgType, profileCompleted)));
    }

    private String roleFor(int orgType, boolean profileCompleted) {
        if (!profileCompleted) {
            return "onboarding";
        }
        return switch (orgType) {
            case 0 -> "operator";
            case 1, 2 -> "agent";
            case 3 -> "proMerchant";
            case 4 -> "liteMerchant";
            case 5 -> "stdMerchant";
            default -> "organization";
        };
    }

    private void validatePassword(String password) {
        int bytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < 8 || bytes > 72) {
            throw new IllegalArgumentException("Password must contain between 8 and 72 UTF-8 bytes");
        }
    }
}
