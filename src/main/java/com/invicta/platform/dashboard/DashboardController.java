package com.invicta.platform.dashboard;

import com.invicta.platform.security.CurrentUser;
import com.invicta.platform.security.OrganizationAccessPolicy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardRepository repository;
    private final OrganizationAccessPolicy access;

    public DashboardController(DashboardRepository repository, OrganizationAccessPolicy access) {
        this.repository = repository;
        this.access = access;
    }

    @GetMapping("/summary")
    public DashboardRepository.DashboardSummary summary(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        access.requireRead(CurrentUser.from(jwt), orgId);
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return repository.summary(orgId, chainId, from, to);
    }
}
