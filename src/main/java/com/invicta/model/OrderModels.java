package com.invicta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderModels {
    private OrderModels() {}

    public record PaymentView(long paymentId, long orgId, int chainId, int currencyId, String outTradeNo,
                              String outPaymentNo, BigDecimal quoteAmount, BigDecimal settlementAmount,
                              BigDecimal expectedAmount, int paymentStatus, String transferHash,
                              String userWalletAddress, String userEmail, LocalDateTime createTime) {}

    public record RechargeView(long id, long orgId, int chainId, Long walletId, int currencyId, int rechargeStatus,
                               BigDecimal rechargeAmount, String transferHash, String fromAddress,
                               String blockTime, LocalDateTime createTime) {}

    public record WithdrawView(long id, long orgId, int chainId, int currencyId, String outTradeNo, int transferType,
                               int transferStatus, BigDecimal transferAmount, String transferAddress,
                               BigDecimal totalAmount, String transferHash, BigDecimal transferFee,
                               LocalDateTime creatTime) {}

    public record Statistics(long totalCount, BigDecimal totalAmount) {}
}
