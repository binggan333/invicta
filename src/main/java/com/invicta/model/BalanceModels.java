package com.invicta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class BalanceModels {
    private BalanceModels() {}

    public record View(long id, long orgId, int chainId, String chainName, Long walletId,
                       String walletAddrHot, String walletAddrCold, int status, String balanceHash,
                       LocalDateTime createTime, String walletAddrManager, Long managerId,
                       Integer balanceType, String balanceId) {}

    public record Line(long id, String balanceId, int currencyId, String currencyAddress, Integer logindex,
                       BigDecimal commissionTotal, BigDecimal commissionAgent, BigDecimal commissionPlatform,
                       BigDecimal amount, LocalDateTime createTime) {}

    public record Detail(View order, List<Line> lines) {}
    public record Statistics(long totalCount, BigDecimal totalAmount, BigDecimal totalCommission) {}
    public record Percentage(BigDecimal selectedBalance, BigDecimal totalBalance, BigDecimal percentage) {}
}
