package com.invicta.service.impl;

import com.invicta.service.SummaryService;

import com.invicta.model.SummaryModels;

import com.invicta.client.LegacyApiClient;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.SummaryRepository;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SummaryServiceImpl implements SummaryService {
    private final SummaryRepository repository;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public SummaryServiceImpl(SummaryRepository repository, OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.repository = repository;
        this.access = access;
        this.legacy = legacy;
    }

    public PageResponse<SummaryModels.View> list(CurrentUser user, long orgId, Integer chainId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        return repository.list(orgId, chainId, status, keyword, from, to, page);
    }

    public SummaryModels.Detail detail(CurrentUser user, long orgId, long id) {
        access.requireRead(user, orgId);
        return repository.detail(orgId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Summary not found"));
    }

    public SummaryModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return repository.statistics(orgId, chainId, status, keyword, from, to);
    }

    public List<SummaryModels.WalletCurrencyBalance> walletCurrencies(CurrentUser user, long orgId, long walletId) {
        access.requireRead(user, orgId);
        return repository.walletBalances(orgId, walletId);
    }

    public Object create(CurrentUser user, long orgId, Map<String, Object> body) {
        access.requireRead(user, orgId);
        return legacy.post("/summary/save", body, Object.class);
    }

    public Object checkStatus(CurrentUser user, long orgId, Map<String, Object> body) {
        access.requireRead(user, orgId);
        return legacy.post("/summary/updateStatus", body, Object.class);
    }
}
