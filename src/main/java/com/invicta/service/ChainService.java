package com.invicta.service;

import com.invicta.model.SystemModels;

import com.invicta.repository.SystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChainService {
    private final SystemRepository repository;
    public ChainService(SystemRepository repository) { this.repository = repository; }
    public List<SystemModels.Chain> list() { return repository.chains(); }
}
