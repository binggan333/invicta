package com.invicta.service;

public interface EmailCodeSender {
    void send(String email, String code);
}
