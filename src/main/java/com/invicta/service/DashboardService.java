package com.invicta.service;

import com.invicta.model.DashboardModels;
import com.invicta.security.CurrentUser;

import java.time.LocalDateTime;

public interface DashboardService {
    DashboardModels.Summary summary(CurrentUser user, long orgId, Integer chainId,
                                    LocalDateTime from, LocalDateTime to);
}
