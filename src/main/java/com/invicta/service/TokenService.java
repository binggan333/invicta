package com.invicta.service;

import com.invicta.model.OrganizationAccount;

import java.time.Instant;

public interface TokenService {
    TokenResponse issue(OrganizationAccount account);

    record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {}
}
