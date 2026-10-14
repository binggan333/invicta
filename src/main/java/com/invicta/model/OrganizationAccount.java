package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "当前登录商户账号")
public record OrganizationAccount(
        @Schema(description = "商户 ID", example = "10001") long orgId,
        @Schema(description = "商户显示名称", example = "Invicta Demo Store") String name,
        @Schema(description = "登录邮箱", example = "owner@example.com") String email,
        @Schema(description = "商户类型编码", example = "5") int orgType,
        @Schema(description = "是否已完成商户资料", example = "true") boolean profileCompleted,
        @Schema(description = "账号角色集合", example = "[\"stdMerchant\"]") Set<String> roles) {}
