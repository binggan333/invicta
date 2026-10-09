package com.invicta.platform.auth;

public interface EmailCodeSender {
    void send(String email, String code);
}
