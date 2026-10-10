package com.invicta.service.impl;

import com.invicta.service.AuthService;
import com.invicta.service.EmailCodeSender;
import com.invicta.service.EmailCodeStore;
import com.invicta.service.TokenService;

import com.invicta.client.LegacyApiClient;
import com.invicta.common.AccountUnavailableException;
import com.invicta.common.EmailAlreadyRegisteredException;
import com.invicta.common.EmailDeliveryException;
import com.invicta.common.TooManyCodeRequestsException;
import com.invicta.common.VerificationCodeInvalidException;
import com.invicta.config.InvictaProperties;
import com.invicta.model.EmailCodePurpose;
import com.invicta.model.OrganizationAccount;
import com.invicta.model.RegistrationRequest;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import com.invicta.util.EmailAddressUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysOrganMapper organizationMapper;
    private final EmailCodeStore codes;
    private final EmailCodeSender sender;
    private final TokenService tokens;
    private final InvictaProperties properties;
    private final LegacyApiClient legacy;
    private final SecureRandom random = new SecureRandom();

    public AuthServiceImpl(SysOrganMapper organizationMapper, EmailCodeStore codes, EmailCodeSender sender,
                           TokenService tokens, InvictaProperties properties, LegacyApiClient legacy) {
        this.organizationMapper = organizationMapper;
        this.codes = codes;
        this.sender = sender;
        this.tokens = tokens;
        this.properties = properties;
        this.legacy = legacy;
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
                .orElseThrow(AccountUnavailableException::new);
        if (!codes.consume(normalizedEmail, EmailCodePurpose.LOGIN, code)) {
            throw new VerificationCodeInvalidException();
        }
        return tokens.issue(account);
    }

    @Override
    public Object register(RegistrationRequest request) {
        String email = EmailAddressUtils.normalize(request.email());
        if (existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        if (!codes.consume(email, EmailCodePurpose.REGISTRATION, request.code())) {
            throw new VerificationCodeInvalidException();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        if (request.details() != null) {
            body.putAll(request.details());
        }
        body.put("email", email);
        body.put("orgName", request.organizationName());
        body.put("regCode", request.registrationCode());
        body.put("chainId", request.chainId());
        body.put("isStake", Boolean.TRUE.equals(request.stakeAccount()));
        return legacy.post(request.type().legacyPath(), body, Object.class);
    }

    private void validatePurpose(String email, EmailCodePurpose purpose) {
        if (purpose == EmailCodePurpose.LOGIN && findActiveByEmail(email).isEmpty()) {
            throw new AccountUnavailableException();
        }
        if (purpose == EmailCodePurpose.REGISTRATION && existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    private Optional<OrganizationAccount> findActiveByEmail(String email) {
        LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
        query.eq(SysOrganEntity::getOrgEmail, email);
        query.eq(SysOrganEntity::getOrgState, 0);
        query.and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                .or().isNull(SysOrganEntity::getIsDelete));

        List<SysOrganEntity> organizations = organizationMapper.selectList(query);
        if (organizations.isEmpty()) {
            return Optional.empty();
        }
        SysOrganEntity organization = organizations.get(0);
        int orgType = organization.getOrgType();
        return Optional.of(new OrganizationAccount(organization.getOrgId(), organization.getOrgName(),
                organization.getOrgEmail(), orgType, Set.of(roleFor(orgType))));
    }

    private boolean existsByEmail(String email) {
        LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
        query.eq(SysOrganEntity::getOrgEmail, email);
        query.and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                .or().isNull(SysOrganEntity::getIsDelete));
        return organizationMapper.selectCount(query) > 0;
    }

    private String roleFor(int orgType) {
        return switch (orgType) {
            case 0 -> "operator";
            case 1, 2 -> "agent";
            case 3 -> "proMerchant";
            case 4 -> "liteMerchant";
            case 5 -> "stdMerchant";
            default -> "organization";
        };
    }
}
