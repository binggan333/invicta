package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.OrderModels;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;

public interface RechargeService {
    PageResponse<OrderModels.RechargeView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page);
    OrderModels.RechargeView detail(CurrentUser user, long orgId, long id);
    OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to);
}
