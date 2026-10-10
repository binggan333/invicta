package com.invicta.controller;

import com.invicta.service.AuthService;
import com.invicta.service.TokenService;
import com.invicta.model.EmailCodePurpose;
import com.invicta.model.RegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/email/request-code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> requestCode(@Valid @RequestBody RequestCode request) {
        authService.requestCode(request.email(), request.purpose());
        return Map.of("message", "验证码已发送");
    }

    @PostMapping("/email/verify")
    public TokenService.TokenResponse verify(@Valid @RequestBody VerifyCode request) {
        return authService.verify(request.email(), request.code());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Object register(@Valid @RequestBody RegistrationRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "accountId", Long.parseLong(jwt.getSubject()),
                "email", jwt.getClaimAsString("email"),
                "name", jwt.getClaimAsString("name"),
                "orgId", jwt.getClaim("orgId"),
                "orgType", jwt.getClaim("orgType"),
                "roles", jwt.getClaimAsStringList("roles")
        );
    }

    public record RequestCode(@NotBlank @Email String email,
                              @NotNull EmailCodePurpose purpose) {}
    public record VerifyCode(@NotBlank @Email String email,
                             @NotBlank @Pattern(regexp = "\\d{6}") String code) {}
}
