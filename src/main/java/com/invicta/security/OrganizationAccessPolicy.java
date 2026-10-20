package com.invicta.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.common.ForbiddenException;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrganizationAccessPolicy {
    private static final Logger log = LoggerFactory.getLogger(OrganizationAccessPolicy.class);
    private final SysOrganMapper organizationMapper;

    public OrganizationAccessPolicy(SysOrganMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public void requireRead(CurrentUser user, long requestedOrgId) {
        if (user.orgId() == requestedOrgId) {
            return;
        }
        requireOperator(user);
        log.info("超管跨机构查询 operatorOrgId={} targetOrgId={}", user.orgId(), requestedOrgId);
    }

    public void requireWrite(CurrentUser user, long requestedOrgId) {
        if (user.operator() || user.orgId() != requestedOrgId) {
            throw new ForbiddenException("You cannot modify this organization");
        }
    }

    public void requireOperator(CurrentUser user) {
        if (!user.operator()) {
            throw new ForbiddenException("Operator access required");
        }
        // 超管权限以当前数据库状态为准，避免账号降权后仍凭旧 JWT 跨商户查询。
        boolean activeOperator = organizationMapper.selectCount(Wrappers.<SysOrganEntity>lambdaQuery()
                .eq(SysOrganEntity::getOrgId, user.orgId())
                .eq(SysOrganEntity::getOrgType, 0)
                .eq(SysOrganEntity::getOrgState, 0)
                .and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                        .or().isNull(SysOrganEntity::getIsDelete))) > 0;
        if (!activeOperator) {
            throw new ForbiddenException("Operator access required");
        }
    }
}
