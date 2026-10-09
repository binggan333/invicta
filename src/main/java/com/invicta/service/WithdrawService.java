package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.OrderModels;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.Map;

public interface WithdrawService {
    PageResponse<OrderModels.WithdrawView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page);
    OrderModels.WithdrawView detail(CurrentUser user, long orgId, long id);
    OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to);
    Object create(CurrentUser user, long orgId, Map<String, Object> body);
    Object approve(CurrentUser user, long orgId, long id, Map<String, Object> body);
    Object confirm(CurrentUser user, long orgId, long id, Map<String, Object> body);
}
