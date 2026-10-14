package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.OrderModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;

public interface WithdrawService {
    PageResponse<OrderModels.WithdrawView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page);
    OrderModels.WithdrawView detail(CurrentUser user, long orgId, long id);
    OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to);
    LegacyApiResponse create(CurrentUser user, long orgId, OperationRequests.WithdrawalCreate request);
    LegacyApiResponse approve(CurrentUser user, long orgId, long id, OperationRequests.WithdrawalApproval request);
    LegacyApiResponse confirm(CurrentUser user, long orgId, long id,
                              OperationRequests.WithdrawalConfirmation request);
}
