package com.invicta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import com.invicta.model.SystemModels;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import com.invicta.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final SysOrganMapper organizationMapper;
    private final OrganizationAccessPolicy access;

    public OrganizationServiceImpl(SysOrganMapper organizationMapper, OrganizationAccessPolicy access) {
        this.organizationMapper = organizationMapper;
        this.access = access;
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

    @Override
    public PageResponse<SystemModels.Organization> list(CurrentUser user, Long orgId, String keyword,
                                                        Integer type, Integer state, PageQuery page) {
        access.requireOperator(user);
        if (orgId != null && orgId <= 0) {
            throw new IllegalArgumentException("orgId must be positive");
        }
        LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
        query.and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                .or().isNull(SysOrganEntity::getIsDelete));
        query.eq(orgId != null, SysOrganEntity::getOrgId, orgId);
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(item -> item.like(SysOrganEntity::getOrgName, value)
                    .or().like(SysOrganEntity::getEmail, value)
                    .or().like(SysOrganEntity::getOrgEmail, value));
        }
        query.eq(type != null, SysOrganEntity::getOrgType, type);
        query.eq(state != null, SysOrganEntity::getOrgState, state);
        Page<SysOrganEntity> request = Page.of(page.page() + 1L, page.size());
        request.addOrder(OrderItem.desc("org_id"));
        IPage<SysOrganEntity> result = organizationMapper.selectPage(request, query);
        List<SystemModels.Organization> items = result.getRecords().stream()
                .map(entity -> new SystemModels.Organization(entity.getOrgId(), entity.getOrgName(),
                        entity.getOrgEmail(), entity.getEmail(), entity.getOrgPhone(), entity.getOrgType(),
                        entity.getOrgParentId(), entity.getOrgState(), entity.getProfileCompleted(),
                        entity.getCreateTime()))
                .toList();
        return PageResponse.of(items, result.getTotal(), page);
    }
}
