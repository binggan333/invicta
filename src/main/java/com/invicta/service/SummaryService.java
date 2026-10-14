package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.SummaryModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.List;

public interface SummaryService {
    PageResponse<SummaryModels.View> list(CurrentUser user, long orgId, Integer chainId,
            Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page);
    SummaryModels.Detail detail(CurrentUser user, long orgId, long id);
    SummaryModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to);
    List<SummaryModels.WalletCurrencyBalance> walletCurrencies(CurrentUser user, long orgId, long walletId);
    LegacyApiResponse create(CurrentUser user, long orgId, OperationRequests.SummaryCreate request);
    LegacyApiResponse checkStatus(CurrentUser user, long orgId, OperationRequests.SummaryStatusCheck request);
}
