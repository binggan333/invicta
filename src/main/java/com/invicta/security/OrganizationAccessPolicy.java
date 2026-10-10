package com.invicta.security;

import com.invicta.common.ForbiddenException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import org.springframework.stereotype.Component;

@Component
public class OrganizationAccessPolicy {
    private final SysOrganMapper organizationMapper;

    public OrganizationAccessPolicy(SysOrganMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public void requireRead(CurrentUser user, long requestedOrgId) {
        if (user.operator() || user.orgId() == requestedOrgId) return;
        if (user.agent()) {
            LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
            query.eq(SysOrganEntity::getOrgId, requestedOrgId);
            query.eq(SysOrganEntity::getOrgParentId, user.orgId());
            query.and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                    .or().isNull(SysOrganEntity::getIsDelete));
            boolean child = organizationMapper.selectCount(query) > 0;
            if (child) return;
        }
        throw new ForbiddenException("You cannot access this organization");
    }
}
