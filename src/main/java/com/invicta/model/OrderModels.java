package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderModels {
    private OrderModels() {}

    @Schema(name = "PaymentView", description = "收单订单")
    public record PaymentView(
            @Schema(description = "收单订单 ID") long paymentId,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "币种 ID") int currencyId,
            @Schema(description = "商户订单号") String outTradeNo,
            @Schema(description = "商户支付单号") String outPaymentNo,
            @Schema(description = "报价金额") BigDecimal quoteAmount,
            @Schema(description = "结算金额") BigDecimal settlementAmount,
            @Schema(description = "应支付金额") BigDecimal expectedAmount,
            @Schema(description = "支付状态编码") int paymentStatus,
            @Schema(description = "链上交易哈希") String transferHash,
            @Schema(description = "付款钱包地址") String userWalletAddress,
            @Schema(description = "付款用户邮箱") String userEmail,
            @Schema(description = "创建时间") LocalDateTime createTime) {}

    @Schema(name = "RechargeView", description = "充值订单")
    public record RechargeView(
            @Schema(description = "充值订单 ID") long id,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "钱包 ID") Long walletId,
            @Schema(description = "币种 ID") int currencyId,
            @Schema(description = "充值状态编码") int rechargeStatus,
            @Schema(description = "充值金额") BigDecimal rechargeAmount,
            @Schema(description = "链上交易哈希") String transferHash,
            @Schema(description = "转出地址") String fromAddress,
            @Schema(description = "链上区块时间") String blockTime,
            @Schema(description = "创建时间") LocalDateTime createTime) {}

    @Schema(name = "WithdrawalView", description = "提现订单")
    public record WithdrawView(
            @Schema(description = "提现订单 ID") long id,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "币种 ID") int currencyId,
            @Schema(description = "商户订单号") String outTradeNo,
            @Schema(description = "转账类型编码") int transferType,
            @Schema(description = "提现状态编码") int transferStatus,
            @Schema(description = "提现金额") BigDecimal transferAmount,
            @Schema(description = "提现接收地址") String transferAddress,
            @Schema(description = "包含手续费的总金额") BigDecimal totalAmount,
            @Schema(description = "链上交易哈希") String transferHash,
            @Schema(description = "提现手续费") BigDecimal transferFee,
            @Schema(description = "创建时间") LocalDateTime creatTime) {}

    @Schema(name = "OrderStatistics", description = "订单统计")
    public record Statistics(
            @Schema(description = "订单总数") long totalCount,
            @Schema(description = "订单总金额") BigDecimal totalAmount) {}
}
