package com.invicta.service;

public interface AuthService {
    void requestCode(String email);
    TokenService.TokenResponse verify(String email, String code);
}
