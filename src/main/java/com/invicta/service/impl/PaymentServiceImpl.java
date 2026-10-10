package com.invicta.service.impl;

import com.invicta.service.PaymentService;

import com.invicta.model.OrderModels;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.OrderPaymentEntity;
import com.invicta.mapper.OrderPaymentMapper;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final OrderPaymentMapper paymentMapper;
    private final OrganizationAccessPolicy access;

    public PaymentServiceImpl(OrderPaymentMapper paymentMapper, OrganizationAccessPolicy access) {
        this.paymentMapper = paymentMapper;
        this.access = access;
    }

    public PageResponse<OrderModels.PaymentView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderPaymentEntity> query = query(orgId, chainId, currencyId, status, keyword, from, to);
        query.orderByDesc(OrderPaymentEntity::getPaymentId);
        IPage<OrderPaymentEntity> result = paymentMapper.selectPage(
                Page.of(page.page() + 1L, page.size()), query);
        List<OrderModels.PaymentView> items = result.getRecords().stream().map(this::toView).toList();
        return PageResponse.of(items, result.getTotal(), page);
    }

    public OrderModels.PaymentView detail(CurrentUser user, long orgId, long paymentId) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrderPaymentEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderPaymentEntity::getPaymentId, paymentId);
        query.eq(OrderPaymentEntity::getOrgId, orgId);
        notDeleted(query);
        OrderPaymentEntity payment = paymentMapper.selectOne(query);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.payment.not-found");
        }
        return toView(payment);
    }

    public OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return paymentMapper.selectStatistics(query(orgId, chainId, currencyId, status, keyword, from, to));
    }

    private LambdaQueryWrapper<OrderPaymentEntity> query(long orgId, Integer chainId, Integer currencyId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderPaymentEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderPaymentEntity::getOrgId, orgId);
        notDeleted(query);
        query.eq(chainId != null, OrderPaymentEntity::getChainId, chainId);
        query.eq(currencyId != null, OrderPaymentEntity::getCurrencyId, currencyId);
        query.eq(status != null, OrderPaymentEntity::getPaymentStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(item -> item.like(OrderPaymentEntity::getOutTradeNo, value)
                    .or().like(OrderPaymentEntity::getOutPaymentNo, value)
                    .or().like(OrderPaymentEntity::getTransferHash, value)
                    .or().like(OrderPaymentEntity::getUserWalletAddress, value));
        }
        query.ge(from != null, OrderPaymentEntity::getCreateTime, from);
        query.lt(to != null, OrderPaymentEntity::getCreateTime, to);
        return query;
    }

    private void notDeleted(LambdaQueryWrapper<OrderPaymentEntity> query) {
        query.and(item -> item.eq(OrderPaymentEntity::getIsDelete, 0)
                .or().isNull(OrderPaymentEntity::getIsDelete));
    }

    private OrderModels.PaymentView toView(OrderPaymentEntity entity) {
        return new OrderModels.PaymentView(entity.getPaymentId(), entity.getOrgId(), entity.getChainId(),
                entity.getCurrencyId(), entity.getOutTradeNo(), entity.getOutPaymentNo(), entity.getQuoteAmount(),
                entity.getSettlementAmount(), entity.getExpectedAmount(), entity.getPaymentStatus(),
                entity.getTransferHash(), entity.getUserWalletAddress(), entity.getUserEmail(), entity.getCreateTime());
    }
}
