package com.invicta.service;

import com.invicta.model.DashboardModels;

import com.invicta.repository.DashboardRepository;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DashboardService {
    private final DashboardRepository repository;
    private final OrganizationAccessPolicy access;

    public DashboardService(DashboardRepository repository, OrganizationAccessPolicy access) {
        this.repository = repository;
        this.access = access;
    }

    public DashboardModels.Summary summary(CurrentUser user, long orgId, Integer chainId,
                                                        LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return repository.summary(orgId, chainId, from, to);
    }
}
