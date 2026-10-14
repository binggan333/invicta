package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 认证接口的请求和响应模型。
 */
public final class AuthModels {
    private AuthModels() {}

    @Schema(description = "操作结果提示")
    public record MessageResponse(
            @Schema(description = "按请求语言返回的提示", example = "验证码已发送") String message) {}

    @Schema(description = "当前登录账号信息")
    public record CurrentAccount(
            @Schema(description = "账号 ID，与商户 ID 一致", example = "10001") long accountId,
            @Schema(description = "登录邮箱", example = "owner@example.com") String email,
            @Schema(description = "商户显示名称", example = "Invicta Demo Store") String name,
            @Schema(description = "商户 ID", example = "10001") long orgId,
            @Schema(description = "商户类型编码", example = "5") int orgType,
            @Schema(description = "是否已完成商户资料", example = "true") boolean profileCompleted,
            @Schema(description = "账号角色列表", example = "[\"stdMerchant\"]") List<String> roles) {}
}
