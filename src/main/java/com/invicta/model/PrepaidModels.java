package com.invicta.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预付费账户、充值地址和流水模型。
 */
public final class PrepaidModels {
    private PrepaidModels() {}

    @Schema(name = "PrepaidAccount", description = "商户预付费账户，金额均为 USDT 展示金额")
    public record Account(
            @Schema(description = "账户 ID") long id,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "可用总余额", example = "120.5") String totalBalance,
            @Schema(description = "冻结金额", example = "0") String frozenAmount,
            @Schema(description = "账户状态：1 正常，2 禁用") Integer status,
            @Schema(description = "累计充值金额", example = "200") String totalRecharge,
            @Schema(description = "累计使用金额", example = "79.5") String totalUsed,
            @Schema(description = "创建时间") LocalDateTime createTime,
            @Schema(description = "更新时间") LocalDateTime updateTime,
            @Schema(description = "余额是否足以继续使用自动化服务") boolean funded) {}

    @Schema(name = "PrepaidRechargeWallet", description = "商户预付费充值地址")
    public record RechargeWallet(
            @Schema(description = "记录 ID") long id,
            @Schema(description = "区块链网络 ID") int chainId,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "充值地址") String walletAddress,
            @Schema(description = "底层钱包 ID") long walletId,
            @Schema(description = "创建时间") LocalDateTime createTime,
            @Schema(description = "更新时间") LocalDateTime updateTime) {}

    @Schema(name = "PrepaidFlow", description = "预付费账户流水")
    public record Flow(
            @Schema(description = "流水 ID") long id,
            @Schema(description = "商户 ID") long orgId,
            @Schema(description = "区块链网络 ID") Integer chainId,
            @Schema(description = "网络名称") String chainName,
            @Schema(description = "业务类型：1 归集，2 平衡，3 提现，4 订阅，5 合约发布，6 充值，7 人工调账，8 KYT，9 预付费提现") int type,
            @Schema(description = "余额动作：1 增加，2 扣款，3 退回，4 补扣") int action,
            @Schema(description = "变动前余额") String beforeBalance,
            @Schema(description = "变动后余额") String afterBalance,
            @Schema(description = "本次变动金额") String changeAmount,
            @Schema(description = "链上交易哈希") String txHash,
            @Schema(description = "业务单号") String bizNo,
            @Schema(description = "创建时间") LocalDateTime createTime,
            @Schema(description = "更新时间") LocalDateTime updateTime,
            @Schema(description = "归集业务包含的用户钱包数量；非归集流水为 0") int userWalletCount) {}

    /**
     * 预付费流水 Excel 导出行。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportRow {
        @ExcelProperty("Biz No")
        private String bizNo;
        @ExcelProperty("Chain ID")
        private Integer chainId;
        @ExcelProperty("Chain Name")
        private String chainName;
        @ExcelProperty("Type")
        private String typeName;
        @ExcelProperty("Action")
        private String actionName;
        @ExcelProperty("Change Amount")
        private String changeAmount;
        @ExcelProperty("Before Balance")
        private String beforeBalance;
        @ExcelProperty("After Balance")
        private String afterBalance;
        @ExcelProperty("Tx Hash")
        private String txHash;
        @ExcelProperty("User Wallet Count")
        private Integer userWalletCount;
        @ExcelProperty("Create Time")
        @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }
}
