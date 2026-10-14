package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class BalanceModels {
    private BalanceModels() {}

    @Schema(name = "BalanceView", description = "资金平衡订单")
    public record View(
            @Schema(description = "平衡订单主键") long id,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "网络名称") String chainName,
            @Schema(description = "钱包 ID") Long walletId,
            @Schema(description = "热钱包地址") String walletAddrHot,
            @Schema(description = "冷钱包地址") String walletAddrCold,
            @Schema(description = "平衡状态编码") int status,
            @Schema(description = "链上交易哈希") String balanceHash,
            @Schema(description = "创建时间") LocalDateTime createTime,
            @Schema(description = "管理钱包地址") String walletAddrManager,
            @Schema(description = "操作管理员 ID") Long managerId,
            @Schema(description = "平衡类型编码") Integer balanceType,
            @Schema(description = "平衡业务编号") String balanceId) {}

    @Schema(name = "BalanceLine", description = "资金平衡明细")
    public record Line(
            @Schema(description = "明细 ID") long id,
            @Schema(description = "平衡业务编号") String balanceId,
            @Schema(description = "币种 ID") int currencyId,
            @Schema(description = "币种合约地址") String currencyAddress,
            @Schema(description = "链上日志索引") Integer logindex,
            @Schema(description = "总手续费") BigDecimal commissionTotal,
            @Schema(description = "代理手续费") BigDecimal commissionAgent,
            @Schema(description = "平台手续费") BigDecimal commissionPlatform,
            @Schema(description = "平衡金额") BigDecimal amount,
            @Schema(description = "创建时间") LocalDateTime createTime) {}

    @Schema(name = "BalanceDetail", description = "资金平衡订单详情")
    public record Detail(
            @Schema(description = "订单信息") View order,
            @Schema(description = "订单明细") List<Line> lines) {}

    @Schema(name = "BalanceStatistics", description = "资金平衡统计")
    public record Statistics(
            @Schema(description = "订单总数") long totalCount,
            @Schema(description = "平衡总金额") BigDecimal totalAmount,
            @Schema(description = "手续费合计") BigDecimal totalCommission) {}

    @Schema(name = "BalancePercentage", description = "指定钱包类型的余额占比")
    public record Percentage(
            @Schema(description = "所选钱包类型余额") BigDecimal selectedBalance,
            @Schema(description = "全部钱包余额") BigDecimal totalBalance,
            @Schema(description = "占比，取值 0 至 100") BigDecimal percentage) {}
}
