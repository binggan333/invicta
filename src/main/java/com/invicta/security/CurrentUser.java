package com.invicta.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record CurrentUser(long orgId, int orgType, boolean profileCompleted, Set<String> roles) {
    public static CurrentUser from(Jwt jwt) {
        Number orgId = jwt.getClaim("orgId");
        Number orgType = jwt.getClaim("orgType");
        Set<String> roles = new HashSet<>(Optional.ofNullable(jwt.getClaimAsStringList("roles")).orElseGet(java.util.List::of));
        return new CurrentUser(orgId.longValue(), orgType.intValue(),
                Boolean.TRUE.equals(jwt.getClaim("profileCompleted")), roles);
    }

    public boolean operator() {
        return orgType == 0 || roles.contains("operator");
    }

    public boolean agent() {
        return orgType == 1 || orgType == 2 || roles.contains("agent");
    }

    public long requestedReadOrgId(Long requestedOrgId) {
        if (requestedOrgId == null) {
            return orgId;
        }
        if (requestedOrgId <= 0) {
            throw new IllegalArgumentException("orgId must be positive");
        }
        // 此处仅确定查询目标；跨商户权限仍由业务服务统一校验。
        return requestedOrgId;
    }
}
