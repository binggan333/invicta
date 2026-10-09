package com.invicta.platform.order;

import com.invicta.platform.common.PageQuery;
import com.invicta.platform.common.PageResponse;
import com.invicta.platform.security.CurrentUser;
import com.invicta.platform.security.OrganizationAccessPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/v1")
public class OrderQueryController {
    private final OrderQueryRepository repository;
    private final OrganizationAccessPolicy access;

    public OrderQueryController(OrderQueryRepository repository, OrganizationAccessPolicy access) {
        this.repository = repository;
        this.access = access;
    }

    @GetMapping("/payments")
    public PageResponse<OrderQueryRepository.PaymentView> payments(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer currencyId, @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        access.requireRead(CurrentUser.from(jwt), orgId);
        return repository.payments(orgId, chainId, currencyId, status, keyword, from, to, new PageQuery(page, size));
    }

    @GetMapping("/recharges")
    public PageResponse<OrderQueryRepository.RechargeView> recharges(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer currencyId, @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        access.requireRead(CurrentUser.from(jwt), orgId);
        return repository.recharges(orgId, chainId, currencyId, status, keyword, from, to, new PageQuery(page, size));
    }

    @GetMapping("/transfers")
    public PageResponse<OrderQueryRepository.TransferView> transfers(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer currencyId, @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        access.requireRead(CurrentUser.from(jwt), orgId);
        return repository.transfers(orgId, chainId, currencyId, status, keyword, from, to, new PageQuery(page, size));
    }
}
