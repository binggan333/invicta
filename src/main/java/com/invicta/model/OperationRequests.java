package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 归集、平衡和提现写操作的请求模型。
 * 商户 ID 始终从登录令牌获取，不允许通过请求参数或请求体覆盖。
 */
public final class OperationRequests {
    private OperationRequests() {}

    @Schema(description = "创建资金平衡订单请求")
    public record BalanceCreate(
            @NotNull @Schema(description = "区块链网络 ID", example = "1") Integer chainId,
            @NotBlank @Schema(description = "链上交易哈希") String balanceHash,
            @Schema(description = "平衡类型编码；不传时由 UTC Pay 使用默认值") Integer balanceType) {}

    @Schema(description = "创建资金归集订单请求")
    public record SummaryCreate(
            @NotBlank @Schema(description = "参与归集的钱包 ID，多个值用英文逗号分隔", example = "1001,1002") String userWalletIds,
            @NotNull @Schema(description = "区块链网络 ID", example = "1") Integer chainId,
            @NotBlank @Schema(description = "链上交易哈希") String transferHash,
            @Schema(description = "归集类型编码；不传时由 UTC Pay 使用默认值") Integer summaryType) {}

    @Schema(description = "检查归集状态请求")
    public record SummaryStatusCheck(
            @NotNull @Schema(description = "区块链网络 ID", example = "1") Integer chainId,
            @NotEmpty @Schema(description = "需要检查的用户钱包 ID 列表", example = "[1001,1002]") List<Integer> userWalletIds) {}

    @Schema(description = "创建提现订单请求")
    public record WithdrawalCreate(
            @NotNull @Schema(description = "区块链网络 ID", example = "1") Integer chainId,
            @NotNull @Schema(description = "币种 ID", example = "1") Integer currencyId,
            @NotBlank @Schema(description = "提现接收地址；多个地址按 UTC Pay 规则使用英文逗号分隔") String transferAddress,
            @NotBlank @Schema(description = "提现金额；多个金额按 UTC Pay 规则使用英文逗号分隔", example = "100.00") String transferAmount,
            @Schema(description = "是否自动提现；不传默认为 true", defaultValue = "true") Boolean automatic,
            @Schema(description = "签名机类型编码") Integer signType) {}

    @Schema(description = "创建提现审批提案请求")
    public record WithdrawalApproval(
            @NotNull @Schema(description = "区块链网络 ID", example = "1") Integer chainId,
            @NotNull @Schema(description = "提案类型编码") Integer proposalType,
            @Positive @Schema(description = "提案开始时间戳（毫秒）") Long startTime,
            @Positive @Schema(description = "提案结束时间戳（毫秒）") Long endTime,
            @Schema(description = "提案管理员类型编码") Integer managerType,
            @Schema(description = "目标钱包地址") String addressTarget,
            @Schema(description = "提案目标值") String value,
            @Schema(description = "提案附加目标值") String value1,
            @Schema(description = "最小投票比例") String minVoteRatio,
            @Schema(description = "ERC-20 合约地址集合") String erc20s,
            @Schema(description = "接收地址集合") String toAddr,
            @Schema(description = "币种金额集合") String amounts,
            @Schema(description = "链上签名") String signature) {}

    @Schema(description = "确认提现审批提案请求")
    public record WithdrawalConfirmation(
            @NotNull @Schema(description = "确认状态编码") Integer status,
            @Schema(description = "链上提案哈希") String proposalHash) {}
}
