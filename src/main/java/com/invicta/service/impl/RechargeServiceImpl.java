package com.invicta.service.impl;

import com.invicta.service.RechargeService;

import com.invicta.model.OrderModels;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.OrderRepository;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class RechargeServiceImpl implements RechargeService {
    private final OrderRepository repository;
    private final OrganizationAccessPolicy access;

    public RechargeServiceImpl(OrderRepository repository, OrganizationAccessPolicy access) {
        this.repository = repository;
        this.access = access;
    }

    public PageResponse<OrderModels.RechargeView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        return repository.recharges(orgId, chainId, currencyId, status, keyword, from, to, page);
    }

    public OrderModels.RechargeView detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        return repository.recharge(orgId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "error.recharge.not-found"));
    }

    public OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return repository.rechargeStatistics(orgId, chainId, currencyId, status, keyword, from, to);
    }
}
