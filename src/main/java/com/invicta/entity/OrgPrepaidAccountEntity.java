package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户预付费账户。
 */
@Data
@TableName("org_prepaid_account")
public class OrgPrepaidAccountEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String totalBalance;
    private String frozenAmount;
    private Integer status;
    private String totalRecharge;
    private String totalUsed;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
