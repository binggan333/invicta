package com.invicta.platform.auth;

import com.invicta.platform.config.InvictaProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final InvictaProperties properties;

    public TokenService(JwtEncoder encoder, InvictaProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public TokenResponse issue(ManagerAccount account) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.auth().tokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("invicta-platform")
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

    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {}
}
