package com.invicta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.invicta.entity.SysOrgChainWalletCurrencyBalanceEntity;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface SysOrgChainWalletCurrencyBalanceMapper extends BaseMapper<SysOrgChainWalletCurrencyBalanceEntity> {
    BigDecimal sumBalance(@Param("orgId") long orgId, @Param("chainId") int chainId,
                          @Param("walletType") String walletType);
}
