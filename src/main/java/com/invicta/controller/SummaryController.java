package com.invicta.controller;

import com.invicta.model.SummaryModels;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.OperationRequests;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.security.CurrentUser;
import com.invicta.service.SummaryService;
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
import java.util.List;

@Tag(name = "资金归集", description = "资金归集订单查询、统计、余额和状态检查")
@Validated
@RestController
@RequestMapping("/api/v1/summaries")
public class SummaryController {
    private final SummaryService service;

    public SummaryController(SummaryService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询资金归集订单")
    @GetMapping
    public PageResponse<SummaryModels.View> list(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "商户 ID", required = true) @RequestParam long orgId,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "归集状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "交易哈希、业务编号或钱包 ID 关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "页码，从 0 开始") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页记录数，最大 200") @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return service.list(CurrentUser.from(jwt), orgId, chainId, status, keyword, from, to, new PageQuery(page, size));
    }

    @Operation(summary = "查询资金归集订单详情")
    @GetMapping("/{id}")
    public SummaryModels.Detail detail(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "商户 ID", required = true) @RequestParam long orgId,
            @Parameter(description = "归集订单 ID", required = true) @PathVariable long id) {
        return service.detail(CurrentUser.from(jwt), orgId, id);
    }

    @Operation(summary = "统计资金归集订单")
    @GetMapping("/statistics")
    public SummaryModels.Statistics statistics(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "商户 ID", required = true) @RequestParam long orgId,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "归集状态编码") @RequestParam(required = false) Integer status,
            @Parameter(description = "交易哈希、业务编号或钱包 ID 关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.statistics(CurrentUser.from(jwt), orgId, chainId, status, keyword, from, to);
    }

    @Operation(summary = "查询钱包币种余额")
    @GetMapping("/wallets/{walletId}/currencies")
    public List<SummaryModels.WalletCurrencyBalance> walletCurrencies(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "商户 ID", required = true) @RequestParam long orgId,
            @Parameter(description = "钱包 ID", required = true) @PathVariable long walletId) {
        return service.walletCurrencies(CurrentUser.from(jwt), orgId, walletId);
    }

    @Operation(summary = "提交资金归集订单", description = "Invicta 校验权限后将写操作转发给 UTC Pay。")
    @PostMapping
    public LegacyApiResponse create(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "商户 ID", required = true) @RequestParam long orgId,
            @Valid @RequestBody OperationRequests.SummaryCreate request) {
        return service.create(CurrentUser.from(jwt), orgId, request);
    }

    @Operation(summary = "检查并更新资金归集状态", description = "通过 UTC Pay 查询链上余额并更新归集状态。")
    @PostMapping("/status-checks")
    public LegacyApiResponse checkStatus(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "商户 ID", required = true) @RequestParam long orgId,
            @Valid @RequestBody OperationRequests.SummaryStatusCheck request) {
        return service.checkStatus(CurrentUser.from(jwt), orgId, request);
    }
}
