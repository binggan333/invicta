package com.invicta.controller;

import com.invicta.model.SystemModels;

import com.invicta.service.ChainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "基础数据-网络", description = "区块链网络基础数据")
@RestController
@RequestMapping("/api/v1/chains")
public class ChainController {
    private final ChainService service;
    public ChainController(ChainService service) { this.service = service; }

    @Operation(summary = "查询网络列表")
    @GetMapping
    public List<SystemModels.Chain> list() { return service.list(); }
}
