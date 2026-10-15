package com.invicta.service;

import com.invicta.model.LegacyApiResponse;
import com.invicta.model.StrategyModels;
import com.invicta.security.CurrentUser;

public interface StrategyService {
    StrategyModels.View get(CurrentUser user, long orgId, int chainId);

    LegacyApiResponse saveSummary(CurrentUser user, long orgId, StrategyModels.SummaryRequest request);

    LegacyApiResponse saveWithdraw(CurrentUser user, long orgId, StrategyModels.WithdrawRequest request);

    LegacyApiResponse saveBalance(CurrentUser user, long orgId, StrategyModels.BalanceRequest request);
}
