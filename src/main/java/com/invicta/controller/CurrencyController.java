package com.invicta.controller;

import com.invicta.model.SystemModels;

import com.invicta.repository.SystemRepository;
import com.invicta.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
public class CurrencyController {
    private final CurrencyService service;
    public CurrencyController(CurrencyService service) { this.service = service; }

    @GetMapping
    public List<SystemModels.Currency> list(@RequestParam(required = false) Integer chainId) {
        return service.list(chainId);
    }
}
