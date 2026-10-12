package com.invicta.service;

import com.invicta.model.EmailCodePurpose;
import com.invicta.model.OrganizationProfileRequest;
import com.invicta.model.RegistrationRequest;

public interface AuthService {
    void requestCode(String email, EmailCodePurpose purpose);
    TokenService.TokenResponse verify(String email, String code);
    TokenService.TokenResponse loginWithPassword(String email, String password);
    TokenService.TokenResponse register(RegistrationRequest request);
    TokenService.TokenResponse completeProfile(long orgId, OrganizationProfileRequest request);
}
