package com.invicta.controller;

import com.invicta.model.OrderModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.security.CurrentUser;
import com.invicta.service.WithdrawService;
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

@Tag(name = "提现", description = "提现订单查询、统计、申请和审批")
@Validated
@RestController
@RequestMapping("/api/v1/withdraw")
public class WithdrawController {
    private final WithdrawService service;

    public WithdrawController(WithdrawService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询提现订单")
    @GetMapping("/list")
    public PageResponse<OrderModels.WithdrawView> list(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "币种 ID") @RequestParam(required = false) Integer currencyId,
            @Parameter(description = "提现状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "商户订单号、交易哈希或提现地址关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "页码，从 0 开始") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页记录数，最大 200") @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.list(user, user.orgId(), chainId, currencyId, status, keyword, from, to, new PageQuery(page, size));
    }

    @Operation(summary = "查询提现订单详情")
    @GetMapping("/detail")
    public OrderModels.WithdrawView detail(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "提现订单 ID", required = true) @RequestParam("id") long id) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.detail(user, user.orgId(), id);
    }

    @Operation(summary = "统计提现订单")
    @GetMapping("/statistics")
    public OrderModels.Statistics statistics(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "币种 ID") @RequestParam(required = false) Integer currencyId,
            @Parameter(description = "提现状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "商户订单号、交易哈希或提现地址关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.statistics(user, user.orgId(), chainId, currencyId, status, keyword, from, to);
    }

    @Operation(summary = "提交提现申请", description = "Invicta 校验权限后将写操作转发给 UTC Pay。")
    @PostMapping("/apply")
    public LegacyApiResponse create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OperationRequests.WithdrawalCreate request) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.create(user, user.orgId(), request);
    }

    @Operation(summary = "为提现订单创建审批提案")
    @PostMapping("/approve")
    public LegacyApiResponse approve(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "提现订单 ID", required = true) @RequestParam("id") long id,
            @Valid @RequestBody OperationRequests.WithdrawalApproval request) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.approve(user, user.orgId(), id, request);
    }

    @Operation(summary = "确认提现审批提案")
    @PostMapping("/proposal/confirm")
    public LegacyApiResponse confirm(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "审批提案 ID", required = true) @RequestParam("id") long id,
            @Valid @RequestBody OperationRequests.WithdrawalConfirmation request) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.confirm(user, user.orgId(), id, request);
    }
}
