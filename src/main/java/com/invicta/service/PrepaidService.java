package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.PrepaidModels;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.List;

public interface PrepaidService {
    PrepaidModels.Account account(CurrentUser user, long orgId);

    List<PrepaidModels.RechargeWallet> rechargeWallets(CurrentUser user, long orgId, Integer chainId);

    List<PrepaidModels.RechargeWallet> createRechargeWallets(CurrentUser user, long orgId);

    List<PrepaidModels.RechargeWallet> refreshRechargeWallets(CurrentUser user, long orgId);

    PageResponse<PrepaidModels.Flow> flows(CurrentUser user, long orgId, Integer chainId,
                                           LocalDateTime from, LocalDateTime to, PageQuery page);

    long countFlows(CurrentUser user, long orgId, Integer chainId, LocalDateTime from, LocalDateTime to);

    List<PrepaidModels.ExportRow> exportPage(CurrentUser user, long orgId, Integer chainId,
                                             LocalDateTime from, LocalDateTime to, int page, int size);
}
