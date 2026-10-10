package com.invicta.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.invicta.entity.OrderSummaryEntity;
import com.invicta.model.SummaryModels;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderSummaryMapper extends BaseMapper<OrderSummaryEntity> {
    SummaryModels.Statistics selectStatistics(@Param(Constants.WRAPPER) Wrapper<OrderSummaryEntity> query);

    List<SummaryModels.WalletCurrencyBalance> selectWalletBalances(@Param("orgId") long orgId,
                                                                   @Param("walletId") long walletId);
}
