package com.invicta.platform.withdrawal;

import com.invicta.platform.common.PageQuery;
import com.invicta.platform.common.PageResponse;
import com.invicta.platform.legacy.LegacyApiClient;
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
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/withdrawals")
public class WithdrawalController {
    private final OrderQueryRepository repository;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public WithdrawalController(OrderQueryRepository repository, OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.repository = repository;
        this.access = access;
        this.legacy = legacy;
    }

    @GetMapping
    public PageResponse<OrderQueryRepository.TransferView> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer currencyId, @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        require(jwt, orgId);
        return repository.transfers(orgId, chainId, currencyId, status, keyword, from, to, new PageQuery(page, size));
    }

    @GetMapping("/{id}")
    public OrderQueryRepository.TransferView detail(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId,
                                                     @PathVariable long id) {
        require(jwt, orgId);
        return repository.transfer(orgId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Withdrawal not found"));
    }

    @GetMapping("/statistics")
    public OrderQueryRepository.OrderStatistics statistics(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer currencyId, @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        require(jwt, orgId);
        return repository.transferStatistics(orgId, chainId, currencyId, status, keyword, from, to);
    }

    @PostMapping
    public Object create(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @RequestBody Map<String, Object> body) {
        require(jwt, orgId);
        return legacy.post("/transfer/apply", body, Object.class);
    }

    @PostMapping("/{id}/approve")
    public Object approve(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @PathVariable long id,
                          @RequestBody Map<String, Object> body) {
        require(jwt, orgId);
        return legacy.post("/transfer/approval", withId(body, id), Object.class);
    }

    @PostMapping("/{id}/confirm")
    public Object confirm(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @PathVariable long id,
                          @RequestBody Map<String, Object> body) {
        require(jwt, orgId);
        return legacy.post("/transfer/confirm", withId(body, id), Object.class);
    }

    private Map<String, Object> withId(Map<String, Object> body, long id) {
        Map<String, Object> request = new java.util.LinkedHashMap<>(body);
        request.putIfAbsent("id", id);
        return request;
    }

    private void require(Jwt jwt, long orgId) { access.requireRead(CurrentUser.from(jwt), orgId); }
}
