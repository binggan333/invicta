package com.invicta.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record CurrentUser(long managerId, long orgId, int managerType, Set<String> roles) {
    public static CurrentUser from(Jwt jwt) {
        Number orgId = jwt.getClaim("orgId");
        Number managerType = jwt.getClaim("managerType");
        Set<String> roles = new HashSet<>(Optional.ofNullable(jwt.getClaimAsStringList("roles")).orElseGet(java.util.List::of));
        return new CurrentUser(Long.parseLong(jwt.getSubject()), orgId.longValue(), managerType.intValue(), roles);
    }

    public boolean operator() {
        return managerType == 0 || roles.contains("operator");
    }

    public boolean agent() {
        return managerType == 3 || roles.contains("agent");
    }
}
