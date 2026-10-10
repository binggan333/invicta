package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_recharge")
public class OrderRechargeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Integer chainId;
    private Long walletId;
    private Integer currencyId;
    private Integer rechargeStatus;
    private BigDecimal rechargeAmount;
    private String transferHash;
    private String fromAddress;
    private String blockTime;
    private LocalDateTime createTime;
    private Integer isDelete;
}
