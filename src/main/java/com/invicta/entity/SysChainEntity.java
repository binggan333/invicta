package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_chain")
public class SysChainEntity {
    @TableId(value = "chain_id", type = IdType.AUTO)
    private Integer chainId;
    private String chainName;
    private String chainSymbol;
    private String outId;
    private String blockExplorer;
    private Integer chainType;
    private Boolean isDefault;
    private Integer oSort;
    private Boolean isDelete;
}
