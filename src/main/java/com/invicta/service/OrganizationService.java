package com.invicta.service;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.SystemModels;
import com.invicta.security.CurrentUser;

public interface OrganizationService {
    PageResponse<SystemModels.Organization> list(CurrentUser user, String keyword,
            Integer type, Integer state, PageQuery page);
}
