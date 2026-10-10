package com.invicta.service.impl;

import com.invicta.service.ChainService;

import com.invicta.model.SystemModels;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.entity.SysChainEntity;
import com.invicta.mapper.SysChainMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainServiceImpl implements ChainService {
    private final SysChainMapper chainMapper;
    public ChainServiceImpl(SysChainMapper chainMapper) { this.chainMapper = chainMapper; }

    public List<SystemModels.Chain> list() {
        LambdaQueryWrapper<SysChainEntity> query = Wrappers.lambdaQuery();
        query.and(item -> item.eq(SysChainEntity::getIsDelete, false)
                .or().isNull(SysChainEntity::getIsDelete));
        query.orderByAsc(SysChainEntity::getOSort, SysChainEntity::getChainId);
        return chainMapper.selectList(query).stream()
                .map(entity -> new SystemModels.Chain(entity.getChainId(), entity.getChainName(),
                        entity.getChainSymbol(), entity.getOutId(), entity.getBlockExplorer(),
                        entity.getChainType(), entity.getIsDefault()))
                .toList();
    }
}
