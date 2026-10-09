package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.OrderModels;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;

public interface PaymentService {
    PageResponse<OrderModels.PaymentView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page);
    OrderModels.PaymentView detail(CurrentUser user, long orgId, long paymentId);
    OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to);
}
