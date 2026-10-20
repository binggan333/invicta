package com.invicta.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.PrepaidModels;
import com.invicta.security.CurrentUser;
import com.invicta.service.PrepaidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Tag(name = "预付费", description = "预付费账户、充值地址、余额流水和数据导出")
@Validated
@RestController
@RequestMapping("/api/v1/prepaid")
public class PrepaidController {
    private static final int EXPORT_PAGE_SIZE = 2_000;
    private static final int MAX_EXPORT_COUNT = 100_000;

    private final PrepaidService service;

    public PrepaidController(PrepaidService service) {
        this.service = service;
    }

    @Operation(summary = "查询预付费账户")
    @GetMapping("/account")
    public PrepaidModels.Account account(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.account(user, user.orgId());
    }

    @Operation(summary = "查询预付费充值地址", description = "每条记录对应一个区块链网络。向地址充值后，由 UTC Pay 的链上监听服务自动入账。")
    @GetMapping("/recharge-wallet/list")
    public List<PrepaidModels.RechargeWallet> rechargeWallets(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.rechargeWallets(user, user.orgId(), chainId);
    }

    @Operation(summary = "创建预付费充值账户", description = "通过 UTC Pay 原有逻辑创建各链充值地址和预付费账户；重复调用不会重复创建。")
    @PostMapping("/recharge-wallet/create")
    public List<PrepaidModels.RechargeWallet> createRechargeWallets(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.createRechargeWallets(user, user.orgId());
    }

    @Operation(summary = "更换预付费充值地址", description = "废弃当前地址并通过 UTC Pay 重新生成各链充值地址。")
    @PostMapping("/recharge-wallet/refresh")
    public List<PrepaidModels.RechargeWallet> refreshRechargeWallets(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.refreshRechargeWallets(user, user.orgId());
    }

    @Operation(summary = "分页查询预付费流水")
    @GetMapping("/flow/list")
    public PageResponse<PrepaidModels.Flow> flows(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "开始时间（包含），ISO-8601") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "页码，从 0 开始") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页记录数，最大 200") @RequestParam(defaultValue = "20")
            @Min(1) @Max(200) int size) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.flows(user, user.orgId(), chainId, from, to, new PageQuery(page, size));
    }

    @Operation(summary = "导出预付费流水", description = "导出 XLSX 文件，时间范围不能超过一年，最多导出 100000 条。")
    @GetMapping("/flow/export")
    public void export(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "区块链网络 ID") @RequestParam(required = false) Integer chainId,
            @Parameter(description = "开始时间（包含），ISO-8601", required = true) @RequestParam
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "结束时间（不包含），ISO-8601", required = true) @RequestParam
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            HttpServletResponse response) throws IOException {
        validateExportRange(from, to);
        CurrentUser user = CurrentUser.from(jwt);
        if (service.countFlows(user, user.orgId(), chainId, from, to) > MAX_EXPORT_COUNT) {
            throw new ResponseStatusException(BAD_REQUEST, "error.export.count-exceeded");
        }
        String filename = "prepaid-flow-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));

        ExcelWriter writer = EasyExcel.write(response.getOutputStream(), PrepaidModels.ExportRow.class)
                .autoCloseStream(false).build();
        try {
            WriteSheet sheet = EasyExcel.writerSheet("prepaidData").build();
            int page = 1;
            while (true) {
                List<PrepaidModels.ExportRow> rows = service.exportPage(
                        user, user.orgId(), chainId, from, to, page, EXPORT_PAGE_SIZE);
                if (rows.isEmpty()) {
                    break;
                }
                writer.write(rows, sheet);
                if (rows.size() < EXPORT_PAGE_SIZE) {
                    break;
                }
                page++;
            }
        } finally {
            writer.finish();
        }
    }

    private void validateExportRange(LocalDateTime from, LocalDateTime to) {
        if (!to.isAfter(from)) {
            throw new ResponseStatusException(BAD_REQUEST, "error.export.time-order");
        }
        if (Duration.between(from, to).compareTo(Duration.ofDays(366)) > 0) {
            throw new ResponseStatusException(BAD_REQUEST, "error.export.range-exceeded");
        }
    }
}
