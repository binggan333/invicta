package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_payment")
public class OrderPaymentEntity {
    @TableId(value = "payment_id", type = IdType.AUTO)
    private Long paymentId;
    private Long orgId;
    private Integer chainId;
    private Integer currencyId;
    private String outTradeNo;
    private String outPaymentNo;
    private BigDecimal quoteAmount;
    private BigDecimal settlementAmount;
    private BigDecimal expectedAmount;
    private Integer paymentStatus;
    private String transferHash;
    private String userWalletAddress;
    private String userEmail;
    private LocalDateTime createTime;
    private Integer isDelete;
}
