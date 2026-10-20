package com.invicta.controller;

import com.invicta.model.OrderModels;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.security.CurrentUser;
import com.invicta.service.RechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "充值", description = "充值订单查询与统计")
@Validated
@RestController
@RequestMapping("/api/v1/recharge")
public class RechargeController {
    private final RechargeService service;

    public RechargeController(RechargeService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询充值订单")
    @GetMapping("/list")
    public PageResponse<OrderModels.RechargeView> list(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "币种 ID") @RequestParam(required = false) Integer currencyId,
            @Parameter(description = "充值状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "交易哈希、转出地址或钱包 ID 关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "页码，从 0 开始") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页记录数，最大 200") @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.list(user, user.orgId(), chainId, currencyId, status, keyword, from, to, new PageQuery(page, size));
    }

    @Operation(summary = "查询充值订单详情")
    @GetMapping("/detail")
    public OrderModels.RechargeView detail(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "充值订单 ID", required = true) @RequestParam("id") long id) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.detail(user, user.orgId(), id);
    }

    @Operation(summary = "统计充值订单")
    @GetMapping("/statistics")
    public OrderModels.Statistics statistics(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "币种 ID") @RequestParam(required = false) Integer currencyId,
            @Parameter(description = "充值状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "交易哈希、转出地址或钱包 ID 关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.statistics(user, user.orgId(), chainId, currencyId, status, keyword, from, to);
    }
}
