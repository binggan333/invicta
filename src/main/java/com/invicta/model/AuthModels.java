package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 认证接口的请求和响应模型。
 */
public final class AuthModels {
    private AuthModels() {}

    @Schema(description = "邮箱检查结果：REGISTERED 为 Invicta 登录邮箱，EXISTING_ACCOUNT 为历史商户联系邮箱，AVAILABLE 为可注册")
    public enum EmailStatus {
        REGISTERED,
        EXISTING_ACCOUNT,
        AVAILABLE
    }

    @Schema(description = "邮箱注册状态；仅用于页面引导，注册和登录接口仍会独立校验")
    public record EmailCheckResponse(
            @Schema(description = "邮箱是否已被账号占用；历史商户邮箱也返回 true", example = "true") boolean registered,
            @Schema(description = "REGISTERED：使用邮箱登录；EXISTING_ACCOUNT：填写商户名走已有账户登录；AVAILABLE：可进入注册流程",
                    allowableValues = {"REGISTERED", "EXISTING_ACCOUNT", "AVAILABLE"}, example = "REGISTERED")
            EmailStatus status) {}

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
