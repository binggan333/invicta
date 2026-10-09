package com.invicta.service.impl;

import com.invicta.service.ChainService;

import com.invicta.model.SystemModels;

import com.invicta.repository.SystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainServiceImpl implements ChainService {
    private final SystemRepository repository;
    public ChainServiceImpl(SystemRepository repository) { this.repository = repository; }
    public List<SystemModels.Chain> list() { return repository.chains(); }
}
