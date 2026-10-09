package com.invicta.platform.aggregation;

import com.invicta.platform.common.PageQuery;
import com.invicta.platform.common.PageResponse;
import com.invicta.platform.legacy.LegacyApiClient;
import com.invicta.platform.security.CurrentUser;
import com.invicta.platform.security.OrganizationAccessPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/aggregations")
public class AggregationController {
    private final AggregationRepository repository;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public AggregationController(AggregationRepository repository, OrganizationAccessPolicy access, LegacyApiClient legacy) {
        this.repository = repository; this.access = access; this.legacy = legacy;
    }

    @GetMapping
    public PageResponse<AggregationRepository.AggregationView> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        require(jwt, orgId);
        return repository.list(orgId, chainId, status, keyword, from, to, new PageQuery(page, size));
    }

    @GetMapping("/{id}")
    public AggregationRepository.AggregationDetail detail(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @PathVariable long id) {
        require(jwt, orgId);
        return repository.detail(orgId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aggregation not found"));
    }

    @GetMapping("/statistics")
    public AggregationRepository.Statistics statistics(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        require(jwt, orgId);
        return repository.statistics(orgId, chainId, status, keyword, from, to);
    }

    @GetMapping("/wallets/{walletId}/currencies")
    public List<AggregationRepository.WalletCurrencyBalance> walletCurrencies(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long orgId, @PathVariable long walletId) {
        require(jwt, orgId);
        return repository.walletBalances(orgId, walletId);
    }

    @PostMapping
    public Object create(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @RequestBody Map<String, Object> body) {
        require(jwt, orgId); return legacy.post("/summary/save", body, Object.class);
    }

    @PostMapping("/check-status")
    public Object checkStatus(@AuthenticationPrincipal Jwt jwt, @RequestParam long orgId, @RequestBody Map<String, Object> body) {
        require(jwt, orgId); return legacy.post("/summary/updateStatus", body, Object.class);
    }

    private void require(Jwt jwt, long orgId) { access.requireRead(CurrentUser.from(jwt), orgId); }
}
