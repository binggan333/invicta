package com.invicta.platform.payment;

import com.invicta.platform.common.PageQuery;
import com.invicta.platform.common.PageResponse;
import com.invicta.platform.order.OrderQueryRepository;
import com.invicta.platform.security.CurrentUser;
import com.invicta.platform.security.OrganizationAccessPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final OrderQueryRepository repository;
    private final OrganizationAccessPolicy access;

    public PaymentController(OrderQueryRepository repository, OrganizationAccessPolicy access) {
        this.repository = repository;
        this.access = access;
    }

    @GetMapping
    public PageResponse<OrderQueryRepository.PaymentView> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer currencyId, @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        require(jwt, orgId);
        return repository.payments(orgId, chainId, currencyId, status, keyword, from, to, new PageQuery(page, size));
    }

    @GetMapping("/{paymentId}")
    public OrderQueryRepository.PaymentView detail(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId,
                                                    @PathVariable long paymentId) {
        require(jwt, orgId);
        return repository.payment(orgId, paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @GetMapping("/statistics")
    public OrderQueryRepository.OrderStatistics statistics(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer currencyId, @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        require(jwt, orgId);
        return repository.paymentStatistics(orgId, chainId, currencyId, status, keyword, from, to);
    }

    private void require(Jwt jwt, long orgId) { access.requireRead(CurrentUser.from(jwt), orgId); }
}
