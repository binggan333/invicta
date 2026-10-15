package com.invicta.controller;

import com.invicta.model.DashboardModels;

import com.invicta.security.CurrentUser;
import com.invicta.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "经营看板", description = "收单、充值和提现的聚合指标")
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @Operation(summary = "查询经营看板概览")
    @GetMapping("/summary")
    public DashboardModels.Summary summary(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "商户 ID", required = true) @RequestParam long orgId,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.summary(CurrentUser.from(jwt), orgId, chainId, from, to);
    }
}
