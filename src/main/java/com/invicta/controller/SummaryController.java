package com.invicta.controller;

import com.invicta.model.SummaryModels;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.SummaryRepository;
import com.invicta.security.CurrentUser;
import com.invicta.service.SummaryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/summary")
public class SummaryController {
    private final SummaryService service;

    public SummaryController(SummaryService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SummaryModels.View> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return service.list(CurrentUser.from(jwt), orgId, chainId, status, keyword, from, to, new PageQuery(page, size));
    }

    @GetMapping("/{id}")
    public SummaryModels.Detail detail(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @PathVariable long id) {
        return service.detail(CurrentUser.from(jwt), orgId, id);
    }

    @GetMapping("/statistics")
    public SummaryModels.Statistics statistics(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.statistics(CurrentUser.from(jwt), orgId, chainId, status, keyword, from, to);
    }

    @GetMapping("/wallets/{walletId}/currencies")
    public List<SummaryModels.WalletCurrencyBalance> walletCurrencies(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @PathVariable long walletId) {
        return service.walletCurrencies(CurrentUser.from(jwt), orgId, walletId);
    }

    @PostMapping
    public Object create(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @RequestBody Map<String, Object> body) {
        return service.create(CurrentUser.from(jwt), orgId, body);
    }

    @PostMapping("/check-status")
    public Object checkStatus(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @RequestBody Map<String, Object> body) {
        return service.checkStatus(CurrentUser.from(jwt), orgId, body);
    }
}
