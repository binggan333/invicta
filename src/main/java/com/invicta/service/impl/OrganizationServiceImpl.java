package com.invicta.service.impl;

import com.invicta.service.OrganizationService;

import com.invicta.model.SystemModels;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.SysOrganEntity;
import com.invicta.mapper.SysOrganMapper;
import com.invicta.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final SysOrganMapper organizationMapper;
    public OrganizationServiceImpl(SysOrganMapper organizationMapper) { this.organizationMapper = organizationMapper; }

    public PageResponse<SystemModels.Organization> list(CurrentUser user, String keyword,
            Integer type, Integer state, PageQuery page) {
        LambdaQueryWrapper<SysOrganEntity> query = Wrappers.lambdaQuery();
        query.and(item -> item.eq(SysOrganEntity::getIsDelete, false)
                .or().isNull(SysOrganEntity::getIsDelete));
        if (!user.operator()) {
            if (user.agent()) {
                query.and(item -> item.eq(SysOrganEntity::getOrgId, user.orgId())
                        .or().eq(SysOrganEntity::getOrgParentId, user.orgId()));
            } else {
                query.eq(SysOrganEntity::getOrgId, user.orgId());
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(item -> item.like(SysOrganEntity::getOrgName, value)
                    .or().like(SysOrganEntity::getOrgEmail, value));
        }
        query.eq(type != null, SysOrganEntity::getOrgType, type);
        query.eq(state != null, SysOrganEntity::getOrgState, state);

        Page<SysOrganEntity> request = Page.of(page.page() + 1L, page.size());
        request.addOrder(OrderItem.desc("org_id"));
        IPage<SysOrganEntity> result = organizationMapper.selectPage(request, query);
        List<SystemModels.Organization> items = result.getRecords().stream().map(this::toView).toList();
        return PageResponse.of(items, result.getTotal(), page);
    }

    private SystemModels.Organization toView(SysOrganEntity entity) {
        return new SystemModels.Organization(entity.getOrgId(), entity.getOrgName(), entity.getOrgEmail(),
                entity.getOrgPhone(), entity.getOrgType(), entity.getOrgParentId(), entity.getOrgState(),
                entity.getCreateTime());
    }
}
