package com.invicta.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "邮箱注册请求")
public record RegistrationRequest(
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.invalid}")
        @Schema(description = "唯一登录邮箱", example = "owner@example.com") String email,
        @NotBlank(message = "{validation.code.required}")
        @Pattern(regexp = "\\d{6}", message = "{validation.code.invalid}")
        @Schema(description = "6 位邮箱验证码", example = "123456") String code,
        @NotBlank(message = "{validation.password.required}")
        @Size(min = 8, max = 72, message = "{validation.password.size}")
        @Schema(description = "登录密码，BCrypt 限制为 8 至 72 个 UTF-8 字节", format = "password") String password
) {}
