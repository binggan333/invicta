package com.invicta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class SummaryModels {
    private SummaryModels() {}

    public record View(long id, long orgId, int chainId, String chainName, Long walletId,
                       String walletAddrHot, String walletAddrCold, int status, String summaryHash,
                       LocalDateTime createTime, String walletAddrManager, Long managerId,
                       String userWalletIds, Integer summaryType, String summaryId) {}

    public record Line(long id, String summaryId, Long userWalletId, String userWalletAddress,
                       String coldAddress, int currencyId, String currencyAddress, BigDecimal amount,
                       Integer logindex, LocalDateTime createTime) {}

    public record Detail(View order, List<Line> lines) {}
    public record Statistics(long totalCount, BigDecimal totalAmount) {}
    public record WalletCurrencyBalance(long balanceId, int chainId, long walletId, int currencyId,
                                        String symbol, BigDecimal balance, LocalDateTime updateTime) {}
}
