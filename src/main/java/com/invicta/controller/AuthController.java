package com.invicta.controller;

import com.invicta.service.AuthService;
import com.invicta.service.TokenService;
import com.invicta.model.AuthModels;
import com.invicta.model.EmailCodePurpose;
import com.invicta.model.OrganizationProfileRequest;
import com.invicta.model.RegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证与账号", description = "邮箱验证码、密码登录、注册及当前账号资料")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final MessageSource messages;

    public AuthController(AuthService authService, MessageSource messages) {
        this.authService = authService;
        this.messages = messages;
    }

    @Operation(summary = "发送邮箱验证码", description = "验证码和重复发送冷却时间均为 60 秒；验证码成功使用后立即失效。")
    @SecurityRequirements
    @PostMapping("/email/code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AuthModels.MessageResponse requestCode(@Valid @RequestBody RequestCode request) {
        authService.requestCode(request.email(), request.purpose());
        return new AuthModels.MessageResponse(messages.getMessage("response.verification-code-sent", null,
                LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "使用邮箱验证码登录")
    @SecurityRequirements
    @PostMapping("/email/login")
    public TokenService.TokenResponse verify(@Valid @RequestBody VerifyCode request) {
        return authService.verify(request.email(), request.code());
    }

    @Operation(summary = "为已有商户发送验证码", description = "按商户名称定位尚未设置新登录邮箱的历史商户，并向历史联系邮箱发送验证码。")
    @SecurityRequirements
    @PostMapping("/existing/code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AuthModels.MessageResponse requestExistingCode(@Valid @RequestBody ExistingRequestCode request) {
        authService.requestExistingAccountCode(request.orgName());
        return new AuthModels.MessageResponse(messages.getMessage("response.verification-code-sent", null,
                LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "已有商户验证码登录")
    @SecurityRequirements
    @PostMapping("/existing/login")
    public TokenService.TokenResponse verifyExisting(@Valid @RequestBody ExistingVerifyCode request) {
        return authService.verifyExistingAccount(request.orgName(), request.code());
    }

    @Operation(summary = "使用邮箱和密码登录")
    @SecurityRequirements
    @PostMapping("/password/login")
    public TokenService.TokenResponse passwordLogin(@Valid @RequestBody PasswordLogin request) {
        return authService.loginWithPassword(request.email(), request.password());
    }

    @Operation(summary = "注册新账号", description = "校验注册验证码并通过 UTC Pay 创建待完善商户账号。")
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenService.TokenResponse register(@Valid @RequestBody RegistrationRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "完善当前商户资料")
    @PostMapping("/profile")
    public TokenService.TokenResponse completeProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrganizationProfileRequest request) {
        return authService.completeProfile(Long.parseLong(jwt.getSubject()), request);
    }

    @Operation(summary = "获取当前登录账号")
    @GetMapping("/me")
    public AuthModels.CurrentAccount me(@AuthenticationPrincipal Jwt jwt) {
        Number orgId = jwt.getClaim("orgId");
        Number orgType = jwt.getClaim("orgType");
        return new AuthModels.CurrentAccount(
                Long.parseLong(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                orgId.longValue(),
                orgType.intValue(),
                Boolean.TRUE.equals(jwt.getClaim("profileCompleted")),
                jwt.getClaimAsStringList("roles"));
    }

    @Schema(name = "VerificationCodeRequest", description = "发送验证码请求")
    public record RequestCode(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}")
            @Schema(description = "登录邮箱", example = "owner@example.com") String email,
            @NotNull(message = "{validation.purpose.required}")
            @Schema(description = "验证码用途：LOGIN 登录，REGISTRATION 注册", example = "LOGIN") EmailCodePurpose purpose) {}

    @Schema(name = "EmailCodeLoginRequest", description = "验证码登录请求")
    public record VerifyCode(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}")
            @Schema(description = "登录邮箱", example = "owner@example.com") String email,
            @NotBlank(message = "{validation.code.required}")
            @Pattern(regexp = "\\d{6}", message = "{validation.code.invalid}")
            @Schema(description = "6 位邮箱验证码", example = "123456") String code) {}

    @Schema(name = "PasswordLoginRequest", description = "密码登录请求")
    public record PasswordLogin(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}")
            @Schema(description = "登录邮箱", example = "owner@example.com") String email,
            @NotBlank(message = "{validation.password.required}")
            @Size(min = 8, max = 72, message = "{validation.password.size}")
            @Schema(description = "登录密码", format = "password") String password) {}

    @Schema(name = "ExistingAccountCodeRequest", description = "已有商户验证码发送请求")
    public record ExistingRequestCode(
            @NotBlank(message = "{validation.organization-name.required}")
            @Size(max = 128, message = "{validation.organization-name.size}")
            @Schema(description = "历史商户名称，必须精确匹配", example = "Existing Store") String orgName) {}

    @Schema(name = "ExistingAccountLoginRequest", description = "已有商户验证码登录请求")
    public record ExistingVerifyCode(
            @NotBlank(message = "{validation.organization-name.required}")
            @Size(max = 128, message = "{validation.organization-name.size}")
            @Schema(description = "历史商户名称，必须精确匹配", example = "Existing Store") String orgName,
            @NotBlank(message = "{validation.code.required}")
            @Pattern(regexp = "\\d{6}", message = "{validation.code.invalid}")
            @Schema(description = "6 位邮箱验证码", example = "123456") String code) {}
}
