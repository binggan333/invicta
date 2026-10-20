package com.invicta.controller;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.SystemModels;
import com.invicta.security.CurrentUser;
import com.invicta.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商户", description = "当前机构资料与超管商户列表")
@Validated
@RestController
@RequestMapping("/api/v1/organization")
public class OrganizationController {
    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @Operation(summary = "查询我的商户资料")
    @GetMapping("/me")
    public SystemModels.Organization me(@AuthenticationPrincipal Jwt jwt) {
        return service.me(CurrentUser.from(jwt), jwt.getClaimAsString("email"));
    }

    @Operation(summary = "分页查询机构", description = "仅 UTC Pay 现有运营机构账号可访问。")
    @GetMapping("/list")
    public PageResponse<SystemModels.Organization> list(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "机构 ID") @RequestParam(required = false) Long orgId,
            @Parameter(description = "机构名称、登录邮箱或历史联系邮箱关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "机构类型编码") @RequestParam(required = false) Integer type,
            @Parameter(description = "机构状态编码") @RequestParam(required = false) Integer state,
            @Parameter(description = "页码，从 0 开始") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页记录数，最大 200") @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return service.list(CurrentUser.from(jwt), orgId, keyword, type, state, new PageQuery(page, size));
    }
}
