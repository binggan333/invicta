package com.invicta.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.invicta.entity.OrderBalanceEntity;
import com.invicta.model.BalanceModels;
import org.apache.ibatis.annotations.Param;

public interface OrderBalanceMapper extends BaseMapper<OrderBalanceEntity> {
    BalanceModels.Statistics selectStatistics(@Param(Constants.WRAPPER) Wrapper<OrderBalanceEntity> query);
}
