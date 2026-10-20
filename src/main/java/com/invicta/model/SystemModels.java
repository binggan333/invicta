package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public final class SystemModels {
    private SystemModels() {}

    @Schema(description = "当前登录商户资料")
    public record Organization(
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "商户名称") String orgName,
            @Schema(description = "历史联系邮箱，不作为 Invicta 登录邮箱") String orgEmail,
            @Schema(description = "当前登录邮箱；已有商户可能使用历史联系邮箱登录") String email,
            @Schema(description = "联系电话") String orgPhone,
            @Schema(description = "商户类型编码") Integer orgType,
            @Schema(description = "上级机构 ID") Long orgParentId,
            @Schema(description = "商户状态编码") Integer orgState,
            @Schema(description = "是否已填写商户资料") Boolean profileCompleted,
            @Schema(description = "创建时间") LocalDateTime createTime) {}

    @Schema(description = "区块链网络")
    public record Chain(
            @Schema(description = "网络 ID") int chainId,
            @Schema(description = "网络名称") String chainName,
            @Schema(description = "网络符号") String chainSymbol,
            @Schema(description = "外部网络标识") String outId,
            @Schema(description = "区块浏览器地址") String blockExplorer,
            @Schema(description = "网络类型编码") int chainType,
            @Schema(description = "是否为默认网络") Boolean isDefault) {}

    @Schema(description = "链上币种")
    public record Currency(
            @Schema(description = "币种 ID") int currencyId,
            @Schema(description = "所属网络 ID") int chainId,
            @Schema(description = "币种名称") String name,
            @Schema(description = "币种符号") String symbol,
            @Schema(description = "链上精度") int decimals,
            @Schema(description = "最小提现金额") String minWithdrawAmount,
            @Schema(description = "手续费") String fee,
            @Schema(description = "合约地址；原生币可能为空") String address,
            @Schema(description = "最小有效入账金额") String minValidAmount) {}
}
