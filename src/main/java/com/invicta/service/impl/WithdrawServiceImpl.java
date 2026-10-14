package com.invicta.service.impl;

import com.invicta.service.WithdrawService;

import com.invicta.model.OrderModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.client.LegacyApiClient;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.OrderTransferEntity;
import com.invicta.mapper.OrderTransferMapper;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WithdrawServiceImpl implements WithdrawService {
    private static final Logger log = LoggerFactory.getLogger(WithdrawServiceImpl.class);
    private final OrderTransferMapper transferMapper;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public WithdrawServiceImpl(OrderTransferMapper transferMapper, OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.transferMapper = transferMapper;
        this.access = access;
        this.legacy = legacy;
    }

    public PageResponse<OrderModels.WithdrawView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderTransferEntity> query = query(orgId, chainId, currencyId, status, keyword, from, to);
        query.orderByDesc(OrderTransferEntity::getId);
        IPage<OrderTransferEntity> result = transferMapper.selectPage(
                Page.of(page.page() + 1L, page.size()), query);
        List<OrderModels.WithdrawView> items = result.getRecords().stream().map(this::toView).toList();
        return PageResponse.of(items, result.getTotal(), page);
    }

    public OrderModels.WithdrawView detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderTransferEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderTransferEntity::getId, id);
        query.eq(OrderTransferEntity::getOrgId, orgId);
        query.eq(OrderTransferEntity::getTransferType, 2);
        notDeleted(query);
        OrderTransferEntity transfer = transferMapper.selectOne(query);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.withdraw.not-found");
        }
        return toView(transfer);
    }

    public OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return transferMapper.selectStatistics(query(orgId, chainId, currencyId, status, keyword, from, to));
    }

    public LegacyApiResponse create(CurrentUser user, long orgId, OperationRequests.WithdrawalCreate request) {
        access.requireRead(user, orgId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgId", orgId);
        body.put("chainId", request.chainId());
        body.put("currencyId", request.currencyId());
        body.put("transferAddress", request.transferAddress());
        body.put("transferAmount", request.transferAmount());
        body.put("isAuto", request.automatic() == null || request.automatic());
        body.put("signType", request.signType());
        log.info("提交提现订单 orgId={} chainId={} currencyId={}", orgId, request.chainId(), request.currencyId());
        return legacy.post("/transfer/apply", body, LegacyApiResponse.class);
    }

    public LegacyApiResponse approve(CurrentUser user, long orgId, long id,
                                     OperationRequests.WithdrawalApproval request) {
        access.requireRead(user, orgId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executionOrgId", orgId);
        body.put("targetOrgId", orgId);
        body.put("chainId", request.chainId());
        body.put("proposalType", request.proposalType());
        body.put("startTime", request.startTime());
        body.put("endTime", request.endTime());
        body.put("managerType", request.managerType());
        body.put("addressTarget", request.addressTarget());
        body.put("value", request.value());
        body.put("value1", request.value1());
        body.put("minVoteRatio", request.minVoteRatio());
        body.put("erc20s", request.erc20s());
        body.put("toAddr", request.toAddr());
        body.put("amounts", request.amounts());
        body.put("signature", request.signature());
        body.put("transferIds", List.of(id));
        log.info("创建提现审批提案 orgId={} withdrawalId={} chainId={}", orgId, id, request.chainId());
        return legacy.post("/transfer/approval", body, LegacyApiResponse.class);
    }

    public LegacyApiResponse confirm(CurrentUser user, long orgId, long id,
                                     OperationRequests.WithdrawalConfirmation request) {
        access.requireRead(user, orgId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgId", orgId);
        body.put("proposalId", id);
        body.put("status", request.status());
        body.put("proposalHash", request.proposalHash());
        log.info("确认提现审批提案 orgId={} proposalId={} status={}", orgId, id, request.status());
        return legacy.post("/transfer/confirm", body, LegacyApiResponse.class);
    }

    private LambdaQueryWrapper<OrderTransferEntity> query(long orgId, Integer chainId, Integer currencyId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderTransferEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderTransferEntity::getOrgId, orgId);
        query.eq(OrderTransferEntity::getTransferType, 2);
        notDeleted(query);
        query.eq(chainId != null, OrderTransferEntity::getChainId, chainId);
        query.eq(currencyId != null, OrderTransferEntity::getCurrencyId, currencyId);
        query.eq(status != null, OrderTransferEntity::getTransferStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(item -> item.like(OrderTransferEntity::getOutTradeNo, value)
                    .or().like(OrderTransferEntity::getTransferHash, value)
                    .or().like(OrderTransferEntity::getTransferAddress, value));
        }
        query.ge(from != null, OrderTransferEntity::getCreatTime, from);
        query.lt(to != null, OrderTransferEntity::getCreatTime, to);
        return query;
    }

    private void notDeleted(LambdaQueryWrapper<OrderTransferEntity> query) {
        query.and(item -> item.eq(OrderTransferEntity::getIsDelete, 0)
                .or().isNull(OrderTransferEntity::getIsDelete));
    }

    private OrderModels.WithdrawView toView(OrderTransferEntity entity) {
        return new OrderModels.WithdrawView(entity.getId(), entity.getOrgId(), entity.getChainId(),
                entity.getCurrencyId(), entity.getOutTradeNo(), entity.getTransferType(), entity.getTransferStatus(),
                entity.getTransferAmount(), entity.getTransferAddress(), entity.getTotalAmount(),
                entity.getTransferHash(), entity.getTransferFee(), entity.getCreatTime());
    }
}
