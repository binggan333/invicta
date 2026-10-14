package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class SummaryModels {
    private SummaryModels() {}

    @Schema(name = "SummaryView", description = "资金归集订单")
    public record View(
            @Schema(description = "归集订单主键") long id,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "网络名称") String chainName,
            @Schema(description = "钱包 ID") Long walletId,
            @Schema(description = "热钱包地址") String walletAddrHot,
            @Schema(description = "冷钱包地址") String walletAddrCold,
            @Schema(description = "归集状态编码") int status,
            @Schema(description = "链上交易哈希") String summaryHash,
            @Schema(description = "创建时间") LocalDateTime createTime,
            @Schema(description = "管理钱包地址") String walletAddrManager,
            @Schema(description = "操作管理员 ID") Long managerId,
            @Schema(description = "参与归集的钱包 ID，逗号分隔") String userWalletIds,
            @Schema(description = "归集类型编码") Integer summaryType,
            @Schema(description = "归集业务编号") String summaryId) {}

    @Schema(name = "SummaryLine", description = "资金归集明细")
    public record Line(
            @Schema(description = "明细 ID") long id,
            @Schema(description = "归集业务编号") String summaryId,
            @Schema(description = "用户钱包 ID") Long userWalletId,
            @Schema(description = "用户钱包地址") String userWalletAddress,
            @Schema(description = "冷钱包地址") String coldAddress,
            @Schema(description = "币种 ID") int currencyId,
            @Schema(description = "币种合约地址") String currencyAddress,
            @Schema(description = "归集金额") BigDecimal amount,
            @Schema(description = "链上日志索引") Integer logindex,
            @Schema(description = "创建时间") LocalDateTime createTime) {}

    @Schema(name = "SummaryDetail", description = "资金归集订单详情")
    public record Detail(
            @Schema(description = "订单信息") View order,
            @Schema(description = "订单明细") List<Line> lines) {}

    @Schema(name = "SummaryStatistics", description = "资金归集统计")
    public record Statistics(
            @Schema(description = "订单总数") long totalCount,
            @Schema(description = "归集总金额") BigDecimal totalAmount) {}

    @Schema(name = "WalletCurrencyBalance", description = "钱包币种余额")
    public record WalletCurrencyBalance(
            @Schema(description = "余额记录 ID") long balanceId,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "钱包 ID") long walletId,
            @Schema(description = "币种 ID") int currencyId,
            @Schema(description = "币种符号") String symbol,
            @Schema(description = "可用余额") BigDecimal balance,
            @Schema(description = "更新时间") LocalDateTime updateTime) {}
}
