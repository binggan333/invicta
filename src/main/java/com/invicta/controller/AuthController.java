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

    @Operation(summary = "检查邮箱注册状态", description = "输入邮箱后调用。优先检查唯一登录邮箱 email，再检查历史商户联系邮箱 org_email；历史邮箱可能对应多个商户，因此仍需填写商户名称。仅用于页面引导，注册和登录时会再次校验。")
    @SecurityRequirements
    @PostMapping("/email/check")
    public AuthModels.EmailCheckResponse checkEmail(@Valid @RequestBody EmailCheckRequest request) {
        return authService.checkEmail(request.email());
    }

    @Operation(summary = "发送邮箱验证码", description = "purpose 必填：REGISTRATION 用于新账号注册，对应 /api/v1/auth/register；LOGIN 用于已注册账号验证码登录，对应 /api/v1/auth/email/login。历史商户请使用 /api/v1/auth/existing/code，无需填写 purpose。验证码和重复发送冷却时间均为 60 秒；验证码成功使用后立即失效。")
    @SecurityRequirements
    @PostMapping("/email/code")
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

    @Schema(name = "EmailCheckRequest", description = "检查邮箱是否已注册")
    public record EmailCheckRequest(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}")
            @Schema(description = "待检查的邮箱；会去除首尾空格并按小写匹配", example = "owner@example.com")
            String email) {}

    @Schema(name = "VerificationCodeRequest", description = "发送验证码请求")
    public record RequestCode(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}")
            @Schema(description = "接收验证码的邮箱。注册时填写新邮箱；登录时填写已注册的 Invicta 登录邮箱。历史商户请改用已有商户验证码接口。", example = "owner@example.com") String email,
            @NotNull(message = "{validation.purpose.required}")
            @Schema(description = "必填。REGISTRATION：新账号注册，验证码仅可用于 /api/v1/auth/register；LOGIN：已注册账号验证码登录，验证码仅可用于 /api/v1/auth/email/login。两种验证码不可混用。",
                    allowableValues = {"REGISTRATION", "LOGIN"}, example = "LOGIN") EmailCodePurpose purpose) {}

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
