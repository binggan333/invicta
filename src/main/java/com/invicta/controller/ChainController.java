package com.invicta.controller;

import com.invicta.model.SystemModels;

import com.invicta.service.ChainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chains")
public class ChainController {
    private final ChainService service;
    public ChainController(ChainService service) { this.service = service; }

    @GetMapping
    public List<SystemModels.Chain> list() { return service.list(); }
}
