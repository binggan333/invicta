package com.invicta.service;

import com.invicta.model.EmailCodePurpose;
import com.invicta.model.RegistrationRequest;

public interface AuthService {
    void requestCode(String email, EmailCodePurpose purpose);
    TokenService.TokenResponse verify(String email, String code);
    Object register(RegistrationRequest request);
}
