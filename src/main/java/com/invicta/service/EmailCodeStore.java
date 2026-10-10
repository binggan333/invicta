package com.invicta.service;

import com.invicta.model.EmailCodePurpose;

import java.time.Duration;

public interface EmailCodeStore {
    boolean issue(String email, EmailCodePurpose purpose, String code, Duration ttl, Duration cooldown);
    boolean consume(String email, EmailCodePurpose purpose, String code);
    void invalidate(String email, EmailCodePurpose purpose);
}
