package com.invicta.service;

import com.invicta.model.ManagerAccount;

import java.time.Instant;

public interface TokenService {
    TokenResponse issue(ManagerAccount account);

    record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {}
}
