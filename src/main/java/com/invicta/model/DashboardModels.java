package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DashboardModels {
    private DashboardModels() {}

    @Schema(name = "DashboardSummary", description = "经营看板概览")
    public record Summary(
            @Schema(description = "收单指标") Metrics payments,
            @Schema(description = "充值指标") Metrics recharges,
            @Schema(description = "提现指标") Metrics withdraws,
            @Schema(description = "统计开始时间（包含）") LocalDateTime from,
            @Schema(description = "统计结束时间（不包含）") LocalDateTime to) {}

    @Schema(name = "DashboardMetrics", description = "业务指标")
    public record Metrics(
            @Schema(description = "记录总数") long totalCount,
            @Schema(description = "金额合计") BigDecimal totalAmount) {}
}
