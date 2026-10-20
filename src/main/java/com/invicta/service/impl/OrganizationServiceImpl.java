package com.invicta.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import com.invicta.model.SystemModels;
import com.invicta.security.CurrentUser;
import com.invicta.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final SysOrganMapper organizationMapper;

    public OrganizationServiceImpl(SysOrganMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    @Override
    public SystemModels.Organization me(CurrentUser user, String loginEmail) {
        SysOrganEntity entity = organizationMapper.selectOne(Wrappers.<SysOrganEntity>lambdaQuery()
                .eq(SysOrganEntity::getOrgId, user.orgId())
                .and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                        .or().isNull(SysOrganEntity::getIsDelete)));
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.organization.not-found");
        }
        return new SystemModels.Organization(entity.getOrgId(), entity.getOrgName(), entity.getOrgEmail(),
                loginEmail, entity.getOrgPhone(), entity.getOrgType(), entity.getOrgParentId(),
                entity.getOrgState(), entity.getProfileCompleted(), entity.getCreateTime());
    }
}
