package com.invicta.platform.auth;

import java.time.Duration;

public interface EmailCodeStore {
    void save(String email, String code, Duration ttl);
    boolean consume(String email, String code);
}
