package com.invicta.service.impl;

import com.invicta.service.PaymentService;

import com.invicta.model.OrderModels;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.OrderRepository;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository repository;
    private final OrganizationAccessPolicy access;

    public PaymentServiceImpl(OrderRepository repository, OrganizationAccessPolicy access) {
        this.repository = repository;
        this.access = access;
    }

    public PageResponse<OrderModels.PaymentView> list(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        return repository.payments(orgId, chainId, currencyId, status, keyword, from, to, page);
    }

    public OrderModels.PaymentView detail(CurrentUser user, long orgId, long paymentId) {
        access.requireRead(user, orgId);
        return repository.payment(orgId, paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "error.payment.not-found"));
    }

    public OrderModels.Statistics statistics(CurrentUser user, long orgId, Integer chainId,
            Integer currencyId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        return repository.paymentStatistics(orgId, chainId, currencyId, status, keyword, from, to);
    }
}
