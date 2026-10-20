package com.invicta.service;

import com.invicta.model.SystemModels;
import com.invicta.security.CurrentUser;

public interface OrganizationService {
    SystemModels.Organization me(CurrentUser user, String loginEmail);
}
