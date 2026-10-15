package com.invicta.controller;

import com.invicta.model.SystemModels;

import com.invicta.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "基础数据-币种", description = "链上币种基础数据")
@RestController
@RequestMapping("/api/v1/currency")
public class CurrencyController {
    private final CurrencyService service;
    public CurrencyController(CurrencyService service) { this.service = service; }

    @Operation(summary = "查询币种列表")
    @GetMapping
    public List<SystemModels.Currency> list(
            @Parameter(description = "按区块链网络 ID 筛选") @RequestParam(required = false) Integer chainId) {
        return service.list(chainId);
    }
}
