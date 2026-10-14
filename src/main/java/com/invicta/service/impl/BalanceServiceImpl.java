package com.invicta.service.impl;

import com.invicta.service.BalanceService;

import com.invicta.model.BalanceModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.client.LegacyApiClient;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.OrderBalanceDetailEntity;
import com.invicta.entity.OrderBalanceEntity;
import com.invicta.mapper.OrderBalanceDetailMapper;
import com.invicta.mapper.OrderBalanceMapper;
import com.invicta.mapper.SysOrgChainWalletCurrencyBalanceMapper;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BalanceServiceImpl implements BalanceService {
    private static final Logger log = LoggerFactory.getLogger(BalanceServiceImpl.class);
    private final OrderBalanceMapper balanceMapper;
    private final OrderBalanceDetailMapper detailMapper;
    private final SysOrgChainWalletCurrencyBalanceMapper walletBalanceMapper;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public BalanceServiceImpl(OrderBalanceMapper balanceMapper, OrderBalanceDetailMapper detailMapper,
                              SysOrgChainWalletCurrencyBalanceMapper walletBalanceMapper,
                              OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.balanceMapper = balanceMapper;
        this.detailMapper = detailMapper;
        this.walletBalanceMapper = walletBalanceMapper;
        this.access = access;
        this.legacy = legacy;
    }

    public PageResponse<BalanceModels.View> list(CurrentUser user, long orgId, Integer chainId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderBalanceEntity> query = query(orgId, chainId, status, keyword, from, to);
        query.orderByDesc(OrderBalanceEntity::getCreateTime);
        IPage<OrderBalanceEntity> result = balanceMapper.selectPage(
                Page.of(page.page() + 1L, page.size()), query);
        List<BalanceModels.View> items = result.getRecords().stream().map(this::toView).toList();
        return PageResponse.of(items, result.getTotal(), page);
    }

    public BalanceModels.Detail detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderBalanceEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderBalanceEntity::getId, id);
        query.eq(OrderBalanceEntity::getOrgId, orgId);
        notDeleted(query);
        OrderBalanceEntity order = balanceMapper.selectOne(query);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.balance.not-found");
        }

        LambdaQueryWrapper<OrderBalanceDetailEntity> detailQuery = Wrappers.lambdaQuery();
        detailQuery.eq(OrderBalanceDetailEntity::getBalanceId, order.getBalanceId());
        detailQuery.orderByAsc(OrderBalanceDetailEntity::getId);
        List<BalanceModels.Line> lines = detailMapper.selectList(detailQuery).stream().map(this::toLine).toList();
        return new BalanceModels.Detail(toView(order), lines);
    }

    public BalanceModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return balanceMapper.selectStatistics(query(orgId, chainId, status, keyword, from, to));
    }

    public BalanceModels.Percentage percentage(CurrentUser user, long orgId, int chainId, String walletType) {
        access.requireRead(user, orgId);
        BigDecimal total = walletBalanceMapper.sumBalance(orgId, chainId, null);
        BigDecimal selected = walletBalanceMapper.sumBalance(orgId, chainId, walletType);
        BigDecimal percentage = total.signum() == 0 ? BigDecimal.ZERO
                : selected.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
        return new BalanceModels.Percentage(selected, total, percentage);
    }

    public LegacyApiResponse create(CurrentUser user, long orgId, OperationRequests.BalanceCreate request) {
        access.requireRead(user, orgId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgId", orgId);
        body.put("chainId", request.chainId());
        body.put("balanceHash", request.balanceHash());
        body.put("balanceType", request.balanceType());
        log.info("提交资金平衡订单 orgId={} chainId={}", orgId, request.chainId());
        return legacy.post("/balance/save", body, LegacyApiResponse.class);
    }

    private LambdaQueryWrapper<OrderBalanceEntity> query(long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderBalanceEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderBalanceEntity::getOrgId, orgId);
        notDeleted(query);
        query.eq(chainId != null, OrderBalanceEntity::getChainId, chainId);
        query.eq(status != null, OrderBalanceEntity::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(item -> item.like(OrderBalanceEntity::getBalanceHash, value)
                    .or().like(OrderBalanceEntity::getBalanceId, value)
                    .or().like(OrderBalanceEntity::getWalletAddrHot, value)
                    .or().like(OrderBalanceEntity::getWalletAddrCold, value));
        }
        query.ge(from != null, OrderBalanceEntity::getCreateTime, from);
        query.lt(to != null, OrderBalanceEntity::getCreateTime, to);
        return query;
    }

    private void notDeleted(LambdaQueryWrapper<OrderBalanceEntity> query) {
        query.and(item -> item.eq(OrderBalanceEntity::getIsDelete, 0)
                .or().isNull(OrderBalanceEntity::getIsDelete));
    }

    private BalanceModels.View toView(OrderBalanceEntity entity) {
        return new BalanceModels.View(entity.getId(), entity.getOrgId(), entity.getChainId(), entity.getChainName(),
                entity.getWalletId(), entity.getWalletAddrHot(), entity.getWalletAddrCold(), entity.getStatus(),
                entity.getBalanceHash(), entity.getCreateTime(), entity.getWalletAddrManager(), entity.getManagerId(),
                entity.getBalanceType(), entity.getBalanceId());
    }

    private BalanceModels.Line toLine(OrderBalanceDetailEntity entity) {
        return new BalanceModels.Line(entity.getId(), entity.getBalanceId(), entity.getCurrencyId(),
                entity.getCurrencyAddress(), entity.getLogindex(), entity.getCommissionTotal(),
                entity.getCommissionAgent(), entity.getCommissionPlatform(), entity.getAmount(), entity.getCreateTime());
    }
}
