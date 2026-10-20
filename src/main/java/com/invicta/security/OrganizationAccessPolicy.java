package com.invicta.security;

import com.invicta.common.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class OrganizationAccessPolicy {
    public void requireRead(CurrentUser user, long requestedOrgId) {
        if (user.orgId() != requestedOrgId) {
            throw new ForbiddenException("You cannot access this organization");
        }
    }
}
