package com.invicta.service.impl;

import com.invicta.service.RechargeService;

import com.invicta.model.OrderModels;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.OrderRechargeEntity;
import com.invicta.mapper.OrderRechargeMapper;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RechargeServiceImpl implements RechargeService {
    private final OrderRechargeMapper rechargeMapper;
    private final OrganizationAccessPolicy access;

    public RechargeServiceImpl(OrderRechargeMapper rechargeMapper, OrganizationAccessPolicy access) {
        this.rechargeMapper = rechargeMapper;
        this.access = access;
    }

    public PageResponse<OrderModels.RechargeView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderRechargeEntity> query = query(orgId, chainId, currencyId, status, keyword, from, to);
        query.orderByDesc(OrderRechargeEntity::getId);
        IPage<OrderRechargeEntity> result = rechargeMapper.selectPage(
                Page.of(page.page() + 1L, page.size()), query);
        List<OrderModels.RechargeView> items = result.getRecords().stream().map(this::toView).toList();
        return PageResponse.of(items, result.getTotal(), page);
    }

    public OrderModels.RechargeView detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderRechargeEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderRechargeEntity::getId, id);
        query.eq(OrderRechargeEntity::getOrgId, orgId);
        notDeleted(query);
        OrderRechargeEntity recharge = rechargeMapper.selectOne(query);
        if (recharge == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.recharge.not-found");
        }
        return toView(recharge);
    }

    public OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return rechargeMapper.selectStatistics(query(orgId, chainId, currencyId, status, keyword, from, to));
    }

    private LambdaQueryWrapper<OrderRechargeEntity> query(long orgId, Integer chainId, Integer currencyId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderRechargeEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderRechargeEntity::getOrgId, orgId);
        notDeleted(query);
        query.eq(chainId != null, OrderRechargeEntity::getChainId, chainId);
        query.eq(currencyId != null, OrderRechargeEntity::getCurrencyId, currencyId);
        query.eq(status != null, OrderRechargeEntity::getRechargeStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(item -> item.like(OrderRechargeEntity::getTransferHash, value)
                    .or().like(OrderRechargeEntity::getFromAddress, value));
        }
        query.ge(from != null, OrderRechargeEntity::getCreateTime, from);
        query.lt(to != null, OrderRechargeEntity::getCreateTime, to);
        return query;
    }

    private void notDeleted(LambdaQueryWrapper<OrderRechargeEntity> query) {
        query.and(item -> item.eq(OrderRechargeEntity::getIsDelete, 0)
                .or().isNull(OrderRechargeEntity::getIsDelete));
    }

    private OrderModels.RechargeView toView(OrderRechargeEntity entity) {
        return new OrderModels.RechargeView(entity.getId(), entity.getOrgId(), entity.getChainId(),
                entity.getWalletId(), entity.getCurrencyId(), entity.getRechargeStatus(), entity.getRechargeAmount(),
                entity.getTransferHash(), entity.getFromAddress(), entity.getBlockTime(), entity.getCreateTime());
    }
}
