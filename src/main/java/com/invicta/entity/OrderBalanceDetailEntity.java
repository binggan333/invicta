package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_balance_detail")
public class OrderBalanceDetailEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String balanceId;
    private Integer currencyId;
    private String currencyAddress;
    private Integer logindex;
    private BigDecimal commissionTotal;
    private BigDecimal commissionAgent;
    private BigDecimal commissionPlatform;
    private BigDecimal amount;
    private LocalDateTime createTime;
}
