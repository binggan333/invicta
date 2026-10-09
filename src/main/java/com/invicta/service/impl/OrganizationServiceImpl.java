package com.invicta.service.impl;

import com.invicta.service.OrganizationService;

import com.invicta.model.SystemModels;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.repository.SystemRepository;
import com.invicta.security.CurrentUser;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final SystemRepository repository;
    public OrganizationServiceImpl(SystemRepository repository) { this.repository = repository; }

    public PageResponse<SystemModels.Organization> list(CurrentUser user, String keyword,
            Integer type, Integer state, PageQuery page) {
        return repository.organizations(user, keyword, type, state, page);
    }
}
