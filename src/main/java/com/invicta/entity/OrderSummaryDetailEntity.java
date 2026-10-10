package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_summary_detail")
public class OrderSummaryDetailEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String summaryId;
    private Long userWalletId;
    private String userWalletAddress;
    private String coldAddress;
    private Integer currencyId;
    private String currencyAddress;
    private BigDecimal amount;
    private Integer logindex;
    private LocalDateTime createTime;
}
