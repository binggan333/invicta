package com.invicta.service;

import com.invicta.model.OrganizationAccount;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public interface TokenService {
    TokenResponse issue(OrganizationAccount account);

    @Schema(description = "登录令牌")
    record TokenResponse(
            @Schema(description = "JWT 访问令牌") String accessToken,
            @Schema(description = "令牌类型", example = "Bearer") String tokenType,
            @Schema(description = "令牌过期时间（UTC）") Instant expiresAt) {}
}
