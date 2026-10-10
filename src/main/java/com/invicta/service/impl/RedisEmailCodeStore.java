package com.invicta.service.impl;

import com.invicta.model.EmailCodePurpose;
import com.invicta.service.EmailCodeStore;
import com.invicta.util.EmailAddressUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Component
public class RedisEmailCodeStore implements EmailCodeStore {
    private static final String PREFIX = "invicta:auth:email:";

    private static final DefaultRedisScript<Long> ISSUE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('SET', KEYS[1], '1', 'NX', 'EX', ARGV[1]) then
                redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3])
                return 1
            end
            return 0
            """, Long.class);

    private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>("""
            local value = redis.call('GET', KEYS[1])
            if not value then
                return 0
            end
            if value == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;

    public RedisEmailCodeStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean issue(String email, EmailCodePurpose purpose, String code, Duration ttl, Duration cooldown) {
        String identity = identity(email, purpose);
        Long result = redis.execute(ISSUE_SCRIPT,
                List.of(identity + ":cooldown", identity + ":code"),
                seconds(cooldown), code, seconds(ttl));
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public boolean consume(String email, EmailCodePurpose purpose, String code) {
        Long result = redis.execute(CONSUME_SCRIPT, List.of(identity(email, purpose) + ":code"), code);
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidate(String email, EmailCodePurpose purpose) {
        String identity = identity(email, purpose);
        redis.delete(List.of(identity + ":cooldown", identity + ":code"));
    }

    private String identity(String email, EmailCodePurpose purpose) {
        return PREFIX + purpose.name().toLowerCase(Locale.ROOT) + ':' + EmailAddressUtils.hash(email);
    }

    private String seconds(Duration duration) {
        return Long.toString(Math.max(1, duration.toSeconds()));
    }
}
