package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_currency")
public class SysCurrencyEntity {
    @TableId(value = "currency_id", type = IdType.AUTO)
    private Integer currencyId;
    private Integer chainId;
    private String name;
    private String symbol;
    private Integer decimals;
    private String minWithdrawAmount;
    private String fee;
    private String address;
    private String minValidAmount;
    private Integer oSort;
    private Boolean isDelete;
}
