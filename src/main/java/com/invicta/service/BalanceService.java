package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.BalanceModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;
public interface BalanceService {
    PageResponse<BalanceModels.View> list(CurrentUser user, long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to, PageQuery page);
    BalanceModels.Detail detail(CurrentUser user, long orgId, long id);
    BalanceModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId, Integer status,
            String keyword, LocalDateTime from, LocalDateTime to);
    BalanceModels.Percentage percentage(CurrentUser user, long orgId, int chainId, String walletType);
    LegacyApiResponse create(CurrentUser user, long orgId, OperationRequests.BalanceCreate request);
}
