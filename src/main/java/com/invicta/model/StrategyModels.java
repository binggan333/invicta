package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 自动归集、提现和平衡策略模型。
 */
public final class StrategyModels {
    private StrategyModels() {}

    @Schema(name = "StrategyView", description = "商户在指定网络上的自动化策略")
    public record View(
            @Schema(description = "策略 ID") Long id,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "状态：1 可用，2 禁用") Integer status,
            @Schema(description = "归集 Gas 价格上限") String gasPriceSummary,
            @Schema(description = "提现 Gas 价格上限") String gasPriceWithdraw,
            @Schema(description = "平衡 Gas 价格上限") String gasPriceBalance,
            @Schema(description = "单钱包触发归集金额，USDT 最小单位（10^-6 USDT）") String summarySingleGt,
            @Schema(description = "全部钱包触发归集总金额，USDT 最小单位（10^-6 USDT）") String summaryTotalGt,
            @Schema(description = "单次最多归集钱包数") Integer summaryMaxWallet,
            @Schema(description = "归集频率（秒）") Integer summaryFreq,
            @Schema(description = "污染阻断开关：1 开启，2 关闭") Integer pollutionBlockSwitch,
            @Schema(description = "拉黑分数") Integer blacklistScore,
            @Schema(description = "单次最多提现钱包数") Integer withdrawMaxWallet,
            @Schema(description = "单次最多提现金额，USDT 最小单位（10^-6 USDT）") String withdrawMaxAmount,
            @Schema(description = "提现频率（秒）") Integer withdrawFreq,
            @Schema(description = "冷钱包超过该金额时触发平衡，USDT 最小单位（10^-6 USDT）") String balanceColdGt,
            @Schema(description = "热钱包低于该金额时触发平衡，USDT 最小单位（10^-6 USDT）") String balanceHotLt,
            @Schema(description = "平衡频率（秒）") Integer balanceFreq,
            @Schema(description = "创建时间") LocalDateTime createTime,
            @Schema(description = "更新时间") LocalDateTime updateTime) {}

    public interface BaseRequest {
        Integer chainId();
        Integer status();
    }

    @Schema(name = "SummaryStrategyRequest", description = "归集策略")
    public record SummaryRequest(
            @NotNull(message = "{validation.chain-id.required}")
            @Schema(description = "区块链网络 ID") Integer chainId,
            @Min(value = 1, message = "{validation.strategy-status.invalid}")
            @Max(value = 2, message = "{validation.strategy-status.invalid}")
            @Schema(description = "状态：1 可用，2 禁用") Integer status,
            @Pattern(regexp = "^(\\d+)?$", message = "{validation.positive-integer.invalid}")
            @Size(max = 15, message = "{validation.amount.size}")
            @Schema(description = "单钱包触发归集金额，USDT 最小单位（10^-6 USDT）") String summarySingleGt,
            @Pattern(regexp = "^(\\d+)?$", message = "{validation.positive-integer.invalid}")
            @Size(max = 15, message = "{validation.amount.size}")
            @Schema(description = "全部钱包触发归集总金额，USDT 最小单位（10^-6 USDT）") String summaryTotalGt,
            @Min(value = 0, message = "{validation.wallet-count.invalid}")
            @Max(value = 100, message = "{validation.wallet-count.invalid}")
            @Schema(description = "单次最多归集钱包数，范围 0 至 100") Integer summaryMaxWallet,
            @Min(value = 1, message = "{validation.switch.invalid}")
            @Max(value = 2, message = "{validation.switch.invalid}")
            @Schema(description = "污染阻断开关：1 开启，2 关闭") Integer pollutionBlockSwitch,
            @Min(value = 0, message = "{validation.score.invalid}")
            @Max(value = 100, message = "{validation.score.invalid}")
            @Schema(description = "拉黑分数，范围 0 至 100") Integer blacklistScore,
            @Pattern(regexp = "^(\\d{1,10}(\\.\\d{1,10})?)?$", message = "{validation.gas-price.invalid}")
            @Schema(description = "归集 Gas 价格上限") String gasPriceSummary) implements BaseRequest {}

    @Schema(name = "WithdrawStrategyRequest", description = "提现策略")
    public record WithdrawRequest(
            @NotNull(message = "{validation.chain-id.required}")
            @Schema(description = "区块链网络 ID") Integer chainId,
            @Min(value = 1, message = "{validation.strategy-status.invalid}")
            @Max(value = 2, message = "{validation.strategy-status.invalid}")
            @Schema(description = "状态：1 可用，2 禁用") Integer status,
            @Min(value = 0, message = "{validation.wallet-count.invalid}")
            @Max(value = 100, message = "{validation.wallet-count.invalid}")
            @Schema(description = "单次最多提现钱包数，范围 0 至 100") Integer withdrawMaxWallet,
            @Pattern(regexp = "^(\\d+)?$", message = "{validation.positive-integer.invalid}")
            @Size(max = 15, message = "{validation.amount.size}")
            @Schema(description = "单次最多提现金额，USDT 最小单位（10^-6 USDT）") String withdrawMaxAmount,
            @Pattern(regexp = "^(\\d{1,10}(\\.\\d{1,10})?)?$", message = "{validation.gas-price.invalid}")
            @Schema(description = "提现 Gas 价格上限") String gasPriceWithdraw) implements BaseRequest {}

    @Schema(name = "BalanceStrategyRequest", description = "平衡策略")
    public record BalanceRequest(
            @NotNull(message = "{validation.chain-id.required}")
            @Schema(description = "区块链网络 ID") Integer chainId,
            @Min(value = 1, message = "{validation.strategy-status.invalid}")
            @Max(value = 2, message = "{validation.strategy-status.invalid}")
            @Schema(description = "状态：1 可用，2 禁用") Integer status,
            @Pattern(regexp = "^(\\d+)?$", message = "{validation.positive-integer.invalid}")
            @Size(max = 15, message = "{validation.amount.size}")
            @Schema(description = "冷钱包超过该金额时触发平衡，USDT 最小单位（10^-6 USDT）") String balanceColdGt,
            @Pattern(regexp = "^(\\d+)?$", message = "{validation.positive-integer.invalid}")
            @Size(max = 15, message = "{validation.amount.size}")
            @Schema(description = "热钱包低于该金额时触发平衡，USDT 最小单位（10^-6 USDT）") String balanceHotLt,
            @Pattern(regexp = "^(\\d{1,10}(\\.\\d{1,10})?)?$", message = "{validation.gas-price.invalid}")
            @Schema(description = "平衡 Gas 价格上限") String gasPriceBalance) implements BaseRequest {}
}
