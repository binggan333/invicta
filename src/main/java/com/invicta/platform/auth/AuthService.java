package com.invicta.platform.auth;

import com.invicta.platform.config.InvictaProperties;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthService {
    private final ManagerAccountRepository accounts;
    private final EmailCodeStore codes;
    private final EmailCodeSender sender;
    private final TokenService tokens;
    private final InvictaProperties properties;
    private final SecureRandom random = new SecureRandom();

    public AuthService(ManagerAccountRepository accounts, EmailCodeStore codes, EmailCodeSender sender,
                       TokenService tokens, InvictaProperties properties) {
        this.accounts = accounts;
        this.codes = codes;
        this.sender = sender;
        this.tokens = tokens;
        this.properties = properties;
    }

    public void requestCode(String email) {
        accounts.findActiveByEmail(email).ifPresent(account -> {
            String code = "%06d".formatted(random.nextInt(1_000_000));
            codes.save(account.email(), code, properties.auth().codeTtl());
            sender.send(account.email(), code);
        });
    }

    public TokenService.TokenResponse verify(String email, String code) {
        ManagerAccount account = accounts.findActiveByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired login code"));
        if (!codes.consume(account.email(), code)) {
            throw new IllegalArgumentException("Invalid or expired login code");
        }
        return tokens.issue(account);
    }
}
