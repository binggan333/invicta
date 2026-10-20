package com.invicta.model;

import com.invicta.validation.AllowedOrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "新账号机构资料")
public record OrganizationProfileRequest(
        @NotBlank(message = "{validation.organization-name.required}")
        @Size(max = 128, message = "{validation.organization-name.size}")
        @Schema(description = "商户名称", example = "Invicta Demo Store") String orgName,
        @NotNull(message = "{validation.organization-type.required}")
        @AllowedOrganizationType
        @Schema(description = "机构类型：1 一级代理，3 专业版商户，4 轻量版商户，5 标准版商户；0 运营机构和已废弃的 2 不允许注册选择",
                allowableValues = {"1", "3", "4", "5"}, example = "5") Integer orgType
) {}
