package com.invicta.platform.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
        authService.requestCode(request.email());
        return Map.of("message", "If the account exists, a login code has been sent");
    }

    @PostMapping("/email/verify")
    public TokenService.TokenResponse verify(@Valid @RequestBody VerifyCode request) {
        return authService.verify(request.email(), request.code());
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "managerId", Long.parseLong(jwt.getSubject()),
                "email", jwt.getClaimAsString("email"),
                "name", jwt.getClaimAsString("name"),
                "orgId", jwt.getClaim("orgId"),
                "managerType", jwt.getClaim("managerType"),
                "roles", jwt.getClaimAsStringList("roles")
        );
    }

    public record RequestCode(@NotBlank @Email String email) {}
    public record VerifyCode(@NotBlank @Email String email,
                             @NotBlank @Pattern(regexp = "\\d{6}") String code) {}
}
