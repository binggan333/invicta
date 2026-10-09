package com.invicta.model;

import java.time.LocalDateTime;

public final class SystemModels {
    private SystemModels() {}

    public record Organization(long orgId, String orgName, String orgEmail, String orgPhone,
                               int orgType, Long orgParentId, int orgState, LocalDateTime createTime) {}
    public record Chain(int chainId, String chainName, String chainSymbol, String outId,
                        String blockExplorer, int chainType, Boolean isDefault) {}
    public record Currency(int currencyId, int chainId, String name, String symbol, int decimals,
                           String minWithdrawAmount, String fee, String address, String minValidAmount) {}
}
