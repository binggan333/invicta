package com.invicta.service.impl;

import com.invicta.service.CurrencyService;

import com.invicta.model.SystemModels;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.entity.SysCurrencyEntity;
import com.invicta.mapper.SysCurrencyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyServiceImpl implements CurrencyService {
    private final SysCurrencyMapper currencyMapper;
    public CurrencyServiceImpl(SysCurrencyMapper currencyMapper) { this.currencyMapper = currencyMapper; }

    public List<SystemModels.Currency> list(Integer chainId) {
        LambdaQueryWrapper<SysCurrencyEntity> query = Wrappers.lambdaQuery();
        query.and(item -> item.eq(SysCurrencyEntity::getIsDelete, false)
                .or().isNull(SysCurrencyEntity::getIsDelete));
        query.eq(chainId != null, SysCurrencyEntity::getChainId, chainId);
        query.orderByAsc(SysCurrencyEntity::getOSort, SysCurrencyEntity::getCurrencyId);
        return currencyMapper.selectList(query).stream()
                .map(entity -> new SystemModels.Currency(entity.getCurrencyId(), entity.getChainId(),
                        entity.getName(), entity.getSymbol(), entity.getDecimals(), entity.getMinWithdrawAmount(),
                        entity.getFee(), entity.getAddress(), entity.getMinValidAmount()))
                .toList();
    }
}
