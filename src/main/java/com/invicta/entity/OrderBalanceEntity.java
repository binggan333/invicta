package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_balance")
public class OrderBalanceEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Integer chainId;
    private String chainName;
    private Long walletId;
    private String walletAddrHot;
    private String walletAddrCold;
    private Integer status;
    private String balanceHash;
    private LocalDateTime createTime;
    private String walletAddrManager;
    private Long managerId;
    private Integer balanceType;
    private String balanceId;
    private Integer isDelete;
}
