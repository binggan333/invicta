package com.invicta.controller;

import com.invicta.model.SystemModels;
import com.invicta.security.CurrentUser;
import com.invicta.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商户", description = "查询当前登录商户的资料")
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
}
