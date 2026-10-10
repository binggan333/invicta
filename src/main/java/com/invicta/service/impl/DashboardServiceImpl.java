package com.invicta.service.impl;

import com.invicta.service.DashboardService;

import com.invicta.model.DashboardModels;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.entity.OrderPaymentEntity;
import com.invicta.entity.OrderRechargeEntity;
import com.invicta.entity.OrderTransferEntity;
import com.invicta.mapper.OrderPaymentMapper;
import com.invicta.mapper.OrderRechargeMapper;
import com.invicta.mapper.OrderTransferMapper;
import com.invicta.model.OrderModels;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final OrderPaymentMapper paymentMapper;
    private final OrderRechargeMapper rechargeMapper;
    private final OrderTransferMapper transferMapper;
    private final OrganizationAccessPolicy access;

    public DashboardServiceImpl(OrderPaymentMapper paymentMapper, OrderRechargeMapper rechargeMapper,
                                OrderTransferMapper transferMapper, OrganizationAccessPolicy access) {
        this.paymentMapper = paymentMapper;
        this.rechargeMapper = rechargeMapper;
        this.transferMapper = transferMapper;
        this.access = access;
    }

    public DashboardModels.Summary summary(CurrentUser user, long orgId, Integer chainId,
                                                        LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        OrderModels.Statistics payment = paymentMapper.selectStatistics(paymentQuery(orgId, chainId, from, to));
        OrderModels.Statistics recharge = rechargeMapper.selectStatistics(rechargeQuery(orgId, chainId, from, to));
        OrderModels.Statistics withdraw = transferMapper.selectStatistics(withdrawQuery(orgId, chainId, from, to));
        return new DashboardModels.Summary(toMetrics(payment), toMetrics(recharge), toMetrics(withdraw), from, to);
    }

    private LambdaQueryWrapper<OrderPaymentEntity> paymentQuery(long orgId, Integer chainId,
            LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderPaymentEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderPaymentEntity::getOrgId, orgId);
        query.and(item -> item.eq(OrderPaymentEntity::getIsDelete, 0)
                .or().isNull(OrderPaymentEntity::getIsDelete));
        query.eq(chainId != null, OrderPaymentEntity::getChainId, chainId);
        query.ge(from != null, OrderPaymentEntity::getCreateTime, from);
        query.lt(to != null, OrderPaymentEntity::getCreateTime, to);
        return query;
    }

    private LambdaQueryWrapper<OrderRechargeEntity> rechargeQuery(long orgId, Integer chainId,
            LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderRechargeEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderRechargeEntity::getOrgId, orgId);
        query.and(item -> item.eq(OrderRechargeEntity::getIsDelete, 0)
                .or().isNull(OrderRechargeEntity::getIsDelete));
        query.eq(chainId != null, OrderRechargeEntity::getChainId, chainId);
        query.ge(from != null, OrderRechargeEntity::getCreateTime, from);
        query.lt(to != null, OrderRechargeEntity::getCreateTime, to);
        return query;
    }

    private LambdaQueryWrapper<OrderTransferEntity> withdrawQuery(long orgId, Integer chainId,
            LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<OrderTransferEntity> query = Wrappers.lambdaQuery();
        query.eq(OrderTransferEntity::getOrgId, orgId);
        query.eq(OrderTransferEntity::getTransferType, 2);
        query.and(item -> item.eq(OrderTransferEntity::getIsDelete, 0)
                .or().isNull(OrderTransferEntity::getIsDelete));
        query.eq(chainId != null, OrderTransferEntity::getChainId, chainId);
        query.ge(from != null, OrderTransferEntity::getCreatTime, from);
        query.lt(to != null, OrderTransferEntity::getCreatTime, to);
        return query;
    }

    private DashboardModels.Metrics toMetrics(OrderModels.Statistics statistics) {
        return new DashboardModels.Metrics(statistics.totalCount(), statistics.totalAmount());
    }
}
