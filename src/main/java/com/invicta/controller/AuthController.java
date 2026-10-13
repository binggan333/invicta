package com.invicta.controller;

import com.invicta.service.AuthService;
import com.invicta.service.TokenService;
import com.invicta.model.EmailCodePurpose;
import com.invicta.model.OrganizationProfileRequest;
import com.invicta.model.RegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final MessageSource messages;

    public AuthController(AuthService authService, MessageSource messages) {
        this.authService = authService;
        this.messages = messages;
    }

    @PostMapping("/email/request-code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> requestCode(@Valid @RequestBody RequestCode request) {
        authService.requestCode(request.email(), request.purpose());
        return Map.of("message", messages.getMessage("response.verification-code-sent", null,
                LocaleContextHolder.getLocale()));
    }

    @PostMapping("/email/verify")
    public TokenService.TokenResponse verify(@Valid @RequestBody VerifyCode request) {
        return authService.verify(request.email(), request.code());
    }

    @PostMapping("/existing/request-code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> requestExistingCode(@Valid @RequestBody ExistingRequestCode request) {
        authService.requestExistingAccountCode(request.orgName());
        return Map.of("message", messages.getMessage("response.verification-code-sent", null,
                LocaleContextHolder.getLocale()));
    }

    @PostMapping("/existing/verify")
    public TokenService.TokenResponse verifyExisting(@Valid @RequestBody ExistingVerifyCode request) {
        return authService.verifyExistingAccount(request.orgName(), request.code());
    }

    @PostMapping("/password/login")
    public TokenService.TokenResponse passwordLogin(@Valid @RequestBody PasswordLogin request) {
        return authService.loginWithPassword(request.email(), request.password());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenService.TokenResponse register(@Valid @RequestBody RegistrationRequest request) {
        return authService.register(request);
    }

    @PutMapping("/profile")
    public TokenService.TokenResponse completeProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrganizationProfileRequest request) {
        return authService.completeProfile(Long.parseLong(jwt.getSubject()), request);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "accountId", Long.parseLong(jwt.getSubject()),
                "email", jwt.getClaimAsString("email"),
                "name", jwt.getClaimAsString("name"),
                "orgId", jwt.getClaim("orgId"),
                "orgType", jwt.getClaim("orgType"),
                "profileCompleted", jwt.getClaim("profileCompleted"),
                "roles", jwt.getClaimAsStringList("roles")
        );
    }

    public record RequestCode(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}") String email,
            @NotNull(message = "{validation.purpose.required}") EmailCodePurpose purpose) {}
    public record VerifyCode(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}") String email,
            @NotBlank(message = "{validation.code.required}")
            @Pattern(regexp = "\\d{6}", message = "{validation.code.invalid}") String code) {}
    public record PasswordLogin(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}") String email,
            @NotBlank(message = "{validation.password.required}")
            @Size(min = 8, max = 72, message = "{validation.password.size}") String password) {}
    public record ExistingRequestCode(
            @NotBlank(message = "{validation.organization-name.required}")
            @Size(max = 128, message = "{validation.organization-name.size}") String orgName) {}
    public record ExistingVerifyCode(
            @NotBlank(message = "{validation.organization-name.required}")
            @Size(max = 128, message = "{validation.organization-name.size}") String orgName,
            @NotBlank(message = "{validation.code.required}")
            @Pattern(regexp = "\\d{6}", message = "{validation.code.invalid}") String code) {}
}
