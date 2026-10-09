package com.invicta.controller;

import com.invicta.model.BalanceModels;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.BalanceRepository;
import com.invicta.security.CurrentUser;
import com.invicta.service.BalanceService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/balances")
public class BalanceController {
    private final BalanceService service;

    public BalanceController(BalanceService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<BalanceModels.View> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return service.list(CurrentUser.from(jwt), orgId, chainId, status, keyword, from, to, new PageQuery(page, size));
    }

    @GetMapping("/{id}")
    public BalanceModels.Detail detail(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId,
                                                   @PathVariable long id) {
        return service.detail(CurrentUser.from(jwt), orgId, id);
    }

    @GetMapping("/statistics")
    public BalanceModels.Statistics statistics(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.statistics(CurrentUser.from(jwt), orgId, chainId, status, keyword, from, to);
    }

    @GetMapping("/percentage")
    public BalanceModels.Percentage percentage(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId,
            @RequestParam int chainId, @RequestParam String walletType) {
        return service.percentage(CurrentUser.from(jwt), orgId, chainId, walletType);
    }

    @PostMapping
    public Object create(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @RequestBody Map<String, Object> body) {
        return service.create(CurrentUser.from(jwt), orgId, body);
    }
}
