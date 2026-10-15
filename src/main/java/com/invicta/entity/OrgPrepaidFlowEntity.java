package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户预付费账户流水。
 */
@Data
@TableName("org_prepaid_flow")
public class OrgPrepaidFlowEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Integer chainId;
    private Integer type;
    private Integer action;
    private String beforeBalance;
    private String afterBalance;
    private String changeAmount;
    private String gasFee;
    private String serviceFee;
    private String txHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String bizNo;
}
