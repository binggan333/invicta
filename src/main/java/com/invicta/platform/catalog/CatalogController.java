package com.invicta.platform.catalog;

import com.invicta.platform.common.PageQuery;
import com.invicta.platform.common.PageResponse;
import com.invicta.platform.security.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogRepository repository;

    public CatalogController(CatalogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/organizations")
    public PageResponse<CatalogRepository.OrganizationView> organizations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer state,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return repository.organizations(CurrentUser.from(jwt), keyword, type, state, new PageQuery(page, size));
    }

    @GetMapping("/chains")
    public List<CatalogRepository.ChainView> chains() {
        return repository.chains();
    }

    @GetMapping("/currencies")
    public List<CatalogRepository.CurrencyView> currencies(@RequestParam(required = false) Integer chainId) {
        return repository.currencies(chainId);
    }
}
