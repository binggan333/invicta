package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户在各链上的预付费充值地址。
 */
@Data
@TableName("org_prepaid_recharge_wallet")
public class OrgPrepaidRechargeWalletEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer chainId;
    private Long orgId;
    private String walletAddress;
    private Long walletId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
}
