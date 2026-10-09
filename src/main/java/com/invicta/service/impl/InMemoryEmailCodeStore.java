package com.invicta.service.impl;

import com.invicta.service.EmailCodeStore;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryEmailCodeStore implements EmailCodeStore {
    private final ConcurrentHashMap<String, Entry> codes = new ConcurrentHashMap<>();

    @Override
    public void save(String email, String code, Duration ttl) {
        codes.put(normalize(email), new Entry(code, Instant.now().plus(ttl)));
    }

    @Override
    public boolean consume(String email, String code) {
        Entry entry = codes.remove(normalize(email));
        return entry != null && Instant.now().isBefore(entry.expiresAt()) && entry.code().equals(code);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record Entry(String code, Instant expiresAt) {}
}
