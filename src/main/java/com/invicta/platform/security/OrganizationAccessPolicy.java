package com.invicta.platform.security;

import com.invicta.platform.common.ForbiddenException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrganizationAccessPolicy {
    private final JdbcClient jdbc;

    public OrganizationAccessPolicy(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public void requireRead(CurrentUser user, long requestedOrgId) {
        if (user.operator() || user.orgId() == requestedOrgId) return;
        if (user.agent()) {
            boolean child = jdbc.sql("SELECT COUNT(*) FROM sys_organ WHERE org_id=:orgId AND org_parent_id=:parentId")
                    .param("orgId", requestedOrgId)
                    .param("parentId", user.orgId())
                    .query(Long.class)
                    .single() > 0;
            if (child) return;
        }
        throw new ForbiddenException("You cannot access this organization");
    }
}
