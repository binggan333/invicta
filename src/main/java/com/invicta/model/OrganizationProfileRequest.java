package com.invicta.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "新账号商户资料")
public record OrganizationProfileRequest(
        @NotBlank(message = "{validation.organization-name.required}")
        @Size(max = 128, message = "{validation.organization-name.size}")
        @Schema(description = "商户名称", example = "Invicta Demo Store") String orgName,
        @NotNull(message = "{validation.organization-type.required}")
        @Min(value = 3, message = "{validation.organization-type.invalid}")
        @Max(value = 5, message = "{validation.organization-type.invalid}")
        @Schema(description = "商户类型：3 专业版，4 轻量版，5 标准版", example = "5") Integer orgType
) {}
