package com.invicta.service.impl;

import com.invicta.service.TokenService;

import com.invicta.config.InvictaProperties;
import com.invicta.model.ManagerAccount;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenServiceImpl implements TokenService {
    private final JwtEncoder encoder;
    private final InvictaProperties properties;

    public TokenServiceImpl(JwtEncoder encoder, InvictaProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    @Override
    public TokenResponse issue(ManagerAccount account) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.auth().tokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("invicta")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(Long.toString(account.id()))
                .claim("email", account.email())
                .claim("name", account.name())
                .claim("orgId", account.orgId())
                .claim("managerType", account.managerType())
                .claim("roles", List.copyOf(account.roles()))
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiresAt);
    }
}
