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
import com.invicta.repository.OrganizationAccountRepository;
import com.invicta.util.EmailAddressUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final OrganizationAccountRepository accounts;
    private final EmailCodeStore codes;
    private final EmailCodeSender sender;
    private final TokenService tokens;
    private final InvictaProperties properties;
    private final LegacyApiClient legacy;
    private final SecureRandom random = new SecureRandom();

    public AuthServiceImpl(OrganizationAccountRepository accounts, EmailCodeStore codes, EmailCodeSender sender,
                           TokenService tokens, InvictaProperties properties, LegacyApiClient legacy) {
        this.accounts = accounts;
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
        OrganizationAccount account = accounts.findActiveByEmail(normalizedEmail)
                .orElseThrow(AccountUnavailableException::new);
        if (!codes.consume(normalizedEmail, EmailCodePurpose.LOGIN, code)) {
            throw new VerificationCodeInvalidException();
        }
        return tokens.issue(account);
    }

    @Override
    public Object register(RegistrationRequest request) {
        String email = EmailAddressUtils.normalize(request.email());
        if (accounts.existsByEmail(email)) {
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
        if (purpose == EmailCodePurpose.LOGIN && accounts.findActiveByEmail(email).isEmpty()) {
            throw new AccountUnavailableException();
        }
        if (purpose == EmailCodePurpose.REGISTRATION && accounts.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
    }
}
