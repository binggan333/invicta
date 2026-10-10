package com.invicta.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.invicta.entity.OrderRechargeEntity;
import com.invicta.model.OrderModels;
import org.apache.ibatis.annotations.Param;

public interface OrderRechargeMapper extends BaseMapper<OrderRechargeEntity> {
    OrderModels.Statistics selectStatistics(@Param(Constants.WRAPPER) Wrapper<OrderRechargeEntity> query);
}
