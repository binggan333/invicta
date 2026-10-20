package com.invicta.controller;

import com.invicta.model.BalanceModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.security.CurrentUser;
import com.invicta.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "资金平衡", description = "资金平衡订单查询、统计与提交")
@Validated
@RestController
@RequestMapping("/api/v1/balance")
public class BalanceController {
    private final BalanceService service;

    public BalanceController(BalanceService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询资金平衡订单")
    @GetMapping("/list")
    public PageResponse<BalanceModels.View> list(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "目标商户 ID；仅超管可指定其他商户，不传则查询自己") @RequestParam(required = false) Long orgId,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "平衡状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "交易哈希、业务编号或钱包地址关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "页码，从 0 开始") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页记录数，最大 200") @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.list(user, user.requestedReadOrgId(orgId), chainId, status, keyword, from, to, new PageQuery(page, size));
    }

    @Operation(summary = "查询资金平衡订单详情")
    @GetMapping("/detail")
    public BalanceModels.Detail detail(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "目标商户 ID；仅超管可指定其他商户") @RequestParam(required = false) Long orgId,
            @Parameter(description = "平衡订单 ID", required = true) @RequestParam("id") long id) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.detail(user, user.requestedReadOrgId(orgId), id);
    }

    @Operation(summary = "统计资金平衡订单")
    @GetMapping("/statistics")
    public BalanceModels.Statistics statistics(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "目标商户 ID；仅超管可指定其他商户") @RequestParam(required = false) Long orgId,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "平衡状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "交易哈希、业务编号或钱包地址关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.statistics(user, user.requestedReadOrgId(orgId), chainId, status, keyword, from, to);
    }

    @Operation(summary = "查询钱包类型余额分布")
    @GetMapping("/percentage")
    public BalanceModels.Percentage percentage(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "目标商户 ID；仅超管可指定其他商户") @RequestParam(required = false) Long orgId,
            @Parameter(description = "区块链网络 ID", required = true) @RequestParam int chainId,
            @Parameter(description = "钱包类型", required = true) @RequestParam String walletType) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.percentage(user, user.requestedReadOrgId(orgId), chainId, walletType);
    }

    @Operation(summary = "提交资金平衡订单", description = "Invicta 校验权限后将写操作转发给 UTC Pay。")
    @PostMapping("/create")
    public LegacyApiResponse create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OperationRequests.BalanceCreate request) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.create(user, user.orgId(), request);
    }
}
