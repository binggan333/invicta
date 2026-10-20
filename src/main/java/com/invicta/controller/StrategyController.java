package com.invicta.controller;

import com.invicta.model.LegacyApiResponse;
import com.invicta.model.StrategyModels;
import com.invicta.security.CurrentUser;
import com.invicta.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "自动化策略", description = "自动归集、提现和平衡策略配置")
@RestController
@RequestMapping("/api/v1/strategy")
public class StrategyController {
    private final StrategyService service;

    public StrategyController(StrategyService service) {
        this.service = service;
    }

    @Operation(summary = "查询商户策略")
    @GetMapping("/info")
    public StrategyModels.View get(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "目标商户 ID；仅超管可指定其他商户") @RequestParam(required = false) Long orgId,
            @Parameter(description = "区块链网络 ID", required = true) @RequestParam int chainId) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.get(user, user.requestedReadOrgId(orgId), chainId);
    }

    @Operation(summary = "保存归集策略")
    @PutMapping("/summary")
    public LegacyApiResponse saveSummary(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StrategyModels.SummaryRequest request) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.saveSummary(user, user.orgId(), request);
    }

    @Operation(summary = "保存提现策略")
    @PutMapping("/withdraw")
    public LegacyApiResponse saveWithdraw(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StrategyModels.WithdrawRequest request) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.saveWithdraw(user, user.orgId(), request);
    }

    @Operation(summary = "保存平衡策略")
    @PutMapping("/balance")
    public LegacyApiResponse saveBalance(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StrategyModels.BalanceRequest request) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.saveBalance(user, user.orgId(), request);
    }
}
