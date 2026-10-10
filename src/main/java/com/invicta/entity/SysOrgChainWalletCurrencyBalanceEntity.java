package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("sys_org_chain_walet_currency_balance")
public class SysOrgChainWalletCurrencyBalanceEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Integer chainId;
    private String walletType;
    private Integer currencyId;
    private BigDecimal balance;
    private Integer isDelete;
    @TableField("wallet_id")
    private Long walletId;
}
