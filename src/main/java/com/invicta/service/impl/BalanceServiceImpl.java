package com.invicta.service.impl;

import com.invicta.service.BalanceService;

import com.invicta.model.BalanceModels;

import com.invicta.client.LegacyApiClient;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.BalanceRepository;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BalanceServiceImpl implements BalanceService {
    private final BalanceRepository repository;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public BalanceServiceImpl(BalanceRepository repository, OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.repository = repository;
        this.access = access;
        this.legacy = legacy;
    }

    public PageResponse<BalanceModels.View> list(CurrentUser user, long orgId, Integer chainId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        return repository.list(orgId, chainId, status, keyword, from, to, page);
    }

    public BalanceModels.Detail detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        return repository.detail(orgId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Balance order not found"));
    }

    public BalanceModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return repository.statistics(orgId, chainId, status, keyword, from, to);
    }

    public BalanceModels.Percentage percentage(CurrentUser user, long orgId, int chainId, String walletType) {
        access.requireRead(user, orgId);
        return repository.percentage(orgId, chainId, walletType);
    }

    public Object create(CurrentUser user, long orgId, Map<String, Object> body) {
        access.requireRead(user, orgId);
        return legacy.post("/balance/save", body, Object.class);
    }
}
