package com.invicta.service.impl;

import com.invicta.service.SummaryService;

import com.invicta.model.SummaryModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.client.LegacyApiClient;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.OrderSummaryDetailEntity;
import com.invicta.entity.OrderSummaryEntity;
import com.invicta.mapper.OrderSummaryDetailMapper;
import com.invicta.mapper.OrderSummaryMapper;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SummaryServiceImpl implements SummaryService {
    private static final Logger log = LoggerFactory.getLogger(SummaryServiceImpl.class);
    private final OrderSummaryMapper summaryMapper;
    private final OrderSummaryDetailMapper detailMapper;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public SummaryServiceImpl(OrderSummaryMapper summaryMapper, OrderSummaryDetailMapper detailMapper,
                              OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.summaryMapper = summaryMapper;
        this.detailMapper = detailMapper;
        this.access = access;
        this.legacy = legacy;
    }

    public PageResponse<SummaryModels.View> list(CurrentUser user, long orgId, Integer chainId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderSummaryEntity> query = query(orgId, chainId, status, keyword, from, to);
        query.orderByDesc(OrderSummaryEntity::getCreateTime);
        IPage<OrderSummaryEntity> result = summaryMapper.selectPage(
                Page.of(page.page() + 1L, page.size()), query);
        List<SummaryModels.View> items = result.getRecords().stream().map(this::toView).toList();
        return PageResponse.of(items, result.getTotal(), page);
    }

    public SummaryModels.Detail detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderSummaryEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderSummaryEntity::getId, id);
        query.eq(OrderSummaryEntity::getOrgId, orgId);
        notDeleted(query);
        OrderSummaryEntity order = summaryMapper.selectOne(query);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.summary.not-found");
        }

        LambdaQueryWrapper<OrderSummaryDetailEntity> detailQuery = Wrappers.lambdaQuery();
        detailQuery.eq(OrderSummaryDetailEntity::getSummaryId, order.getSummaryId());
        detailQuery.orderByAsc(OrderSummaryDetailEntity::getId);
        List<SummaryModels.Line> lines = detailMapper.selectList(detailQuery).stream().map(this::toLine).toList();
        return new SummaryModels.Detail(toView(order), lines);
    }

    public SummaryModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return summaryMapper.selectStatistics(query(orgId, chainId, status, keyword, from, to));
    }

    public List<SummaryModels.WalletCurrencyBalance> walletCurrencies(CurrentUser user, long orgId,
                                                                       long walletId, Integer chainId) {
        access.requireRead(user, orgId);
        return summaryMapper.selectWalletBalances(orgId, walletId, chainId);
    }

    public LegacyApiResponse create(CurrentUser user, long orgId, OperationRequests.SummaryCreate request) {
        access.requireWrite(user, orgId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgId", orgId);
        body.put("userWalletIds", request.userWalletIds());
        body.put("chainId", request.chainId());
        body.put("transferHash", request.transferHash());
        body.put("summaryType", request.summaryType());
        log.info("提交资金归集订单 orgId={} chainId={}", orgId, request.chainId());
        return legacy.post("/summary/save", body, LegacyApiResponse.class);
    }

    public LegacyApiResponse checkStatus(CurrentUser user, long orgId,
                                         OperationRequests.SummaryStatusCheck request) {
        access.requireWrite(user, orgId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgId", orgId);
        body.put("chainId", request.chainId());
        body.put("userWalletIds", request.userWalletIds());
        log.info("检查资金归集状态 orgId={} chainId={} walletCount={}",
                orgId, request.chainId(), request.userWalletIds().size());
        return legacy.post("/summary/updateStatus", body, LegacyApiResponse.class);
    }

    private LambdaQueryWrapper<OrderSummaryEntity> query(long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderSummaryEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderSummaryEntity::getOrgId, orgId);
        notDeleted(query);
        query.eq(chainId != null, OrderSummaryEntity::getChainId, chainId);
        query.eq(status != null, OrderSummaryEntity::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(item -> item.like(OrderSummaryEntity::getSummaryHash, value)
                    .or().like(OrderSummaryEntity::getSummaryId, value)
                    .or().apply("FIND_IN_SET({0}, user_wallet_ids)", value));
        }
        query.ge(from != null, OrderSummaryEntity::getCreateTime, from);
        query.lt(to != null, OrderSummaryEntity::getCreateTime, to);
        return query;
    }

    private void notDeleted(LambdaQueryWrapper<OrderSummaryEntity> query) {
        query.and(item -> item.eq(OrderSummaryEntity::getIsDelete, 0)
                .or().isNull(OrderSummaryEntity::getIsDelete));
    }

    private SummaryModels.View toView(OrderSummaryEntity entity) {
        return new SummaryModels.View(entity.getId(), entity.getOrgId(), entity.getChainId(), entity.getChainName(),
                entity.getWalletId(), entity.getWalletAddrHot(), entity.getWalletAddrCold(), entity.getStatus(),
                entity.getSummaryHash(), entity.getCreateTime(), entity.getWalletAddrManager(), entity.getManagerId(),
                entity.getUserWalletIds(), entity.getSummaryType(), entity.getSummaryId());
    }

    private SummaryModels.Line toLine(OrderSummaryDetailEntity entity) {
        return new SummaryModels.Line(entity.getId(), entity.getSummaryId(), entity.getUserWalletId(),
                entity.getUserWalletAddress(), entity.getColdAddress(), entity.getCurrencyId(),
                entity.getCurrencyAddress(), entity.getAmount(), entity.getLogindex(), entity.getCreateTime());
    }
}
