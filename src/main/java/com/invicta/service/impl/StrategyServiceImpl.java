package com.invicta.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.invicta.client.LegacyApiClient;
import com.invicta.entity.OrgStrategyEntity;
import com.invicta.mapper.OrgStrategyMapper;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.StrategyModels;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import com.invicta.service.StrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StrategyServiceImpl implements StrategyService {
    private static final Logger log = LoggerFactory.getLogger(StrategyServiceImpl.class);

    private final OrgStrategyMapper strategyMapper;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public StrategyServiceImpl(OrgStrategyMapper strategyMapper,
                               OrganizationAccessPolicy access,
                               LegacyApiClient legacy) {
        this.strategyMapper = strategyMapper;
        this.access = access;
        this.legacy = legacy;
    }

    @Override
    public StrategyModels.View get(CurrentUser user, long orgId, int chainId) {
        access.requireRead(user, orgId);
        OrgStrategyEntity entity = strategyMapper.selectOne(Wrappers.<OrgStrategyEntity>lambdaQuery()
                .eq(OrgStrategyEntity::getOrgId, orgId)
                .eq(OrgStrategyEntity::getChainId, chainId));
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.strategy.not-found");
        }
        return toView(entity);
    }

    @Override
    public LegacyApiResponse saveSummary(CurrentUser user, long orgId, StrategyModels.SummaryRequest request) {
        access.requireRead(user, orgId);
        Map<String, Object> body = baseBody(orgId, request);
        body.put("summarySingleGt", request.summarySingleGt());
        body.put("summaryTotalGt", request.summaryTotalGt());
        body.put("summaryMaxWallet", request.summaryMaxWallet());
        body.put("pollutionBlockSwitch", request.pollutionBlockSwitch());
        body.put("blacklistScore", request.blacklistScore());
        body.put("gasPriceSummary", request.gasPriceSummary());
        log.info("保存归集策略 orgId={} chainId={}", orgId, request.chainId());
        return legacy.postSigned("/api/invicta/strategy/summary", body, LegacyApiResponse.class);
    }

    @Override
    public LegacyApiResponse saveWithdraw(CurrentUser user, long orgId, StrategyModels.WithdrawRequest request) {
        access.requireRead(user, orgId);
        Map<String, Object> body = baseBody(orgId, request);
        body.put("withdrawMaxWallet", request.withdrawMaxWallet());
        body.put("withdrawMaxAmount", request.withdrawMaxAmount());
        body.put("gasPriceWithdraw", request.gasPriceWithdraw());
        log.info("保存提现策略 orgId={} chainId={}", orgId, request.chainId());
        return legacy.postSigned("/api/invicta/strategy/withdraw", body, LegacyApiResponse.class);
    }

    @Override
    public LegacyApiResponse saveBalance(CurrentUser user, long orgId, StrategyModels.BalanceRequest request) {
        access.requireRead(user, orgId);
        Map<String, Object> body = baseBody(orgId, request);
        body.put("balanceColdGt", request.balanceColdGt());
        body.put("balanceHotLt", request.balanceHotLt());
        body.put("gasPriceBalance", request.gasPriceBalance());
        log.info("保存平衡策略 orgId={} chainId={}", orgId, request.chainId());
        return legacy.postSigned("/api/invicta/strategy/balance", body, LegacyApiResponse.class);
    }

    private Map<String, Object> baseBody(long orgId, StrategyModels.BaseRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgId", orgId);
        body.put("chainId", request.chainId());
        body.put("status", request.status());
        return body;
    }

    private StrategyModels.View toView(OrgStrategyEntity entity) {
        return new StrategyModels.View(entity.getId(), entity.getOrgId(), entity.getChainId(), entity.getStatus(),
                entity.getGasPriceSummary(), entity.getGasPriceWithdraw(), entity.getGasPriceBalance(),
                entity.getSummarySingleGt(), entity.getSummaryTotalGt(), entity.getSummaryMaxWallet(),
                entity.getSummaryFreq(), entity.getPollutionBlockSwitch(), entity.getBlacklistScore(),
                entity.getWithdrawMaxWallet(), entity.getWithdrawMaxAmount(), entity.getWithdrawFreq(),
                entity.getBalanceColdGt(), entity.getBalanceHotLt(), entity.getBalanceFreq(),
                entity.getCreateTime(), entity.getUpdateTime());
    }
}
