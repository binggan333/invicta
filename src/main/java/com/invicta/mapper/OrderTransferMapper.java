package com.invicta.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.invicta.entity.OrderTransferEntity;
import com.invicta.model.OrderModels;
import org.apache.ibatis.annotations.Param;

public interface OrderTransferMapper extends BaseMapper<OrderTransferEntity> {
    OrderModels.Statistics selectStatistics(@Param(Constants.WRAPPER) Wrapper<OrderTransferEntity> query);
}
