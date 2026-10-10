package com.invicta.service.impl;

import com.invicta.service.WithdrawService;

import com.invicta.model.OrderModels;

import com.invicta.client.LegacyApiClient;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.OrderRepository;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WithdrawServiceImpl implements WithdrawService {
    private final OrderRepository repository;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public WithdrawServiceImpl(OrderRepository repository, OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.repository = repository;
        this.access = access;
        this.legacy = legacy;
    }

    public PageResponse<OrderModels.WithdrawView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        return repository.withdraws(orgId, chainId, currencyId, status, keyword, from, to, page);
    }

    public OrderModels.WithdrawView detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        return repository.withdraw(orgId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "error.withdraw.not-found"));
    }

    public OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return repository.withdrawStatistics(orgId, chainId, currencyId, status, keyword, from, to);
    }

    public Object create(CurrentUser user, long orgId, Map<String, Object> body) {
        access.requireRead(user, orgId);
        return legacy.post("/transfer/apply", body, Object.class);
    }

    public Object approve(CurrentUser user, long orgId, long id, Map<String, Object> body) {
        access.requireRead(user, orgId);
        return legacy.post("/transfer/approval", withId(body, id), Object.class);
    }

    public Object confirm(CurrentUser user, long orgId, long id, Map<String, Object> body) {
        access.requireRead(user, orgId);
        return legacy.post("/transfer/confirm", withId(body, id), Object.class);
    }

    private Map<String, Object> withId(Map<String, Object> body, long id) {
        Map<String, Object> request = new LinkedHashMap<>(body);
        request.putIfAbsent("id", id);
        return request;
    }
}
