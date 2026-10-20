package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.SystemModels;
import com.invicta.security.CurrentUser;

public interface OrganizationService {
    SystemModels.Organization me(CurrentUser user, String loginEmail);

    PageResponse<SystemModels.Organization> list(CurrentUser user, Long orgId, String keyword,
                                                 Integer type, Integer state, PageQuery page);
}
