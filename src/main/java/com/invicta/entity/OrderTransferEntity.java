package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_transfer")
public class OrderTransferEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Integer chainId;
    private Integer currencyId;
    private String outTradeNo;
    private Integer transferType;
    private Integer transferStatus;
    private BigDecimal transferAmount;
    private String transferAddress;
    private BigDecimal totalAmount;
    private String transferHash;
    private BigDecimal transferFee;
    private LocalDateTime creatTime;
    private Integer isDelete;
}
