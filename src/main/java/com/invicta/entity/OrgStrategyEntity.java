package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户自动归集、提现和平衡策略。
 */
@Data
@TableName("org_strategy")
public class OrgStrategyEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Integer chainId;
    private Integer status;
    private String gasPriceSummary;
    private String gasPriceWithdraw;
    private String gasPriceBalance;
    private String summarySingleGt;
    private String summaryTotalGt;
    private Integer summaryMaxWallet;
    private Integer summaryFreq;
    private Integer pollutionBlockSwitch;
    private Integer blacklistScore;
    private Integer withdrawMaxWallet;
    private String withdrawMaxAmount;
    private Integer withdrawFreq;
    private String balanceColdGt;
    private String balanceHotLt;
    private Integer balanceFreq;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
