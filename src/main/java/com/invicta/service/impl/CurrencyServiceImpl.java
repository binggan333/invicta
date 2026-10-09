package com.invicta.service.impl;

import com.invicta.service.CurrencyService;

import com.invicta.model.SystemModels;

import com.invicta.repository.SystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyServiceImpl implements CurrencyService {
    private final SystemRepository repository;
    public CurrencyServiceImpl(SystemRepository repository) { this.repository = repository; }
    public List<SystemModels.Currency> list(Integer chainId) { return repository.currencies(chainId); }
}
