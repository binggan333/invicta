package com.invicta.service;

import com.invicta.model.SystemModels;

import com.invicta.repository.SystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {
    private final SystemRepository repository;
    public CurrencyService(SystemRepository repository) { this.repository = repository; }
    public List<SystemModels.Currency> list(Integer chainId) { return repository.currencies(chainId); }
}
