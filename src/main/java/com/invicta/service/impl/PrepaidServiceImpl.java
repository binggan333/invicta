package com.invicta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.client.LegacyApiClient;
import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.entity.OrgPrepaidAccountEntity;
import com.invicta.entity.OrgPrepaidFlowEntity;
import com.invicta.entity.OrgPrepaidRechargeWalletEntity;
import com.invicta.mapper.OrgPrepaidAccountMapper;
import com.invicta.mapper.OrgPrepaidFlowMapper;
import com.invicta.mapper.OrgPrepaidRechargeWalletMapper;
import com.invicta.model.LegacyApiResponse;
import com.invicta.model.PrepaidModels;
import com.invicta.security.CurrentUser;
import com.invicta.security.OrganizationAccessPolicy;
import com.invicta.service.PrepaidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrepaidServiceImpl implements PrepaidService {
    private static final int PREPAID_DECIMALS = 6;
    private static final Logger log = LoggerFactory.getLogger(PrepaidServiceImpl.class);

    private final OrgPrepaidAccountMapper accountMapper;
    private final OrgPrepaidRechargeWalletMapper rechargeWalletMapper;
    private final OrgPrepaidFlowMapper flowMapper;
    private final OrganizationAccessPolicy access;
    private final LegacyApiClient legacy;

    public PrepaidServiceImpl(OrgPrepaidAccountMapper accountMapper,
                              OrgPrepaidRechargeWalletMapper rechargeWalletMapper,
                              OrgPrepaidFlowMapper flowMapper,
                              OrganizationAccessPolicy access,
                              LegacyApiClient legacy) {
        this.accountMapper = accountMapper;
        this.rechargeWalletMapper = rechargeWalletMapper;
        this.flowMapper = flowMapper;
        this.access = access;
        this.legacy = legacy;
    }

    @Override
    public PrepaidModels.Account account(CurrentUser user, long orgId) {
        access.requireRead(user, orgId);
        OrgPrepaidAccountEntity entity = accountMapper.selectOne(
                Wrappers.<OrgPrepaidAccountEntity>lambdaQuery()
                        .eq(OrgPrepaidAccountEntity::getOrgId, orgId));
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.prepaid.account-not-found");
        }
        String totalBalance = displayAmount(entity.getTotalBalance());
        return new PrepaidModels.Account(entity.getId(), entity.getOrgId(), totalBalance,
                displayAmount(entity.getFrozenAmount()), entity.getStatus(),
                displayAmount(entity.getTotalRecharge()), displayAmount(entity.getTotalUsed()),
                entity.getCreateTime(), entity.getUpdateTime(),
                entity.getStatus() != null && entity.getStatus() == 1
                        && new BigDecimal(totalBalance).signum() > 0);
    }

    @Override
    public List<PrepaidModels.RechargeWallet> rechargeWallets(CurrentUser user, long orgId, Integer chainId) {
        access.requireRead(user, orgId);
        return activeWallets(orgId, chainId).stream().map(this::toWallet).toList();
    }

    @Override
    public List<PrepaidModels.RechargeWallet> createRechargeWallets(CurrentUser user, long orgId) {
        access.requireWrite(user, orgId);
        legacy.postSigned("/api/invicta/prepaid/rechargeAddress/create",
                Map.of("orgId", orgId), LegacyApiResponse.class);
        log.info("预付费充值账户创建完成 orgId={}", orgId);
        return activeWallets(orgId).stream().map(this::toWallet).toList();
    }

    @Override
    public List<PrepaidModels.RechargeWallet> refreshRechargeWallets(CurrentUser user, long orgId) {
        access.requireWrite(user, orgId);
        Map<String, Object> body = Map.of("orgId", orgId);
        legacy.postSigned("/api/invicta/prepaid/recharge-wallet/refresh",
                body, LegacyApiResponse.class);
        log.info("预付费充值地址已更新 orgId={}", orgId);
        return activeWallets(orgId).stream().map(this::toWallet).toList();
    }

    @Override
    public PageResponse<PrepaidModels.Flow> flows(CurrentUser user, long orgId, Integer chainId,
                                                   LocalDateTime from, LocalDateTime to, PageQuery page) {
        access.requireRead(user, orgId);
        IPage<PrepaidModels.Flow> result = flowMapper.selectFlowPage(
                Page.of(page.page() + 1L, page.size()), orgId, chainId, from, to);
        List<PrepaidModels.Flow> items = result.getRecords().stream().map(this::displayFlow).toList();
        return PageResponse.of(items, result.getTotal(), page);
    }

    @Override
    public long countFlows(CurrentUser user, long orgId, Integer chainId,
                           LocalDateTime from, LocalDateTime to) {
        access.requireRead(user, orgId);
        LambdaQueryWrapper<OrgPrepaidFlowEntity> query = Wrappers.lambdaQuery();
        query.eq(OrgPrepaidFlowEntity::getOrgId, orgId);
        query.eq(chainId != null, OrgPrepaidFlowEntity::getChainId, chainId);
        query.ge(from != null, OrgPrepaidFlowEntity::getCreateTime, from);
        query.lt(to != null, OrgPrepaidFlowEntity::getCreateTime, to);
        return flowMapper.selectCount(query);
    }

    @Override
    public List<PrepaidModels.ExportRow> exportPage(CurrentUser user, long orgId, Integer chainId,
                                                    LocalDateTime from, LocalDateTime to,
                                                    int page, int size) {
        access.requireRead(user, orgId);
        Page<PrepaidModels.Flow> pageParam = new Page<>(page, size, false);
        return flowMapper.selectFlowPage(pageParam, orgId, chainId, from, to).getRecords().stream()
                .map(this::displayFlow)
                .map(item -> new PrepaidModels.ExportRow(item.bizNo(), item.chainId(), item.chainName(),
                        typeName(item.type()), actionName(item.action()), item.changeAmount(),
                        item.beforeBalance(), item.afterBalance(), item.txHash(),
                        item.userWalletCount(), item.createTime()))
                .toList();
    }

    private List<OrgPrepaidRechargeWalletEntity> activeWallets(long orgId) {
        return activeWallets(orgId, null);
    }

    private List<OrgPrepaidRechargeWalletEntity> activeWallets(long orgId, Integer chainId) {
        LambdaQueryWrapper<OrgPrepaidRechargeWalletEntity> query = Wrappers.lambdaQuery();
        query.eq(OrgPrepaidRechargeWalletEntity::getOrgId, orgId);
        query.eq(chainId != null, OrgPrepaidRechargeWalletEntity::getChainId, chainId);
        query.eq(OrgPrepaidRechargeWalletEntity::getIsDelete, 0);
        query.orderByAsc(OrgPrepaidRechargeWalletEntity::getChainId);
        return rechargeWalletMapper.selectList(query);
    }

    private PrepaidModels.RechargeWallet toWallet(OrgPrepaidRechargeWalletEntity entity) {
        return new PrepaidModels.RechargeWallet(entity.getId(), entity.getChainId(), entity.getOrgId(),
                entity.getWalletAddress(), entity.getWalletId(), entity.getCreateTime(), entity.getUpdateTime());
    }

    private PrepaidModels.Flow displayFlow(PrepaidModels.Flow flow) {
        return new PrepaidModels.Flow(flow.id(), flow.orgId(), flow.chainId(), flow.chainName(),
                flow.type(), flow.action(), displayAmount(flow.beforeBalance()), displayAmount(flow.afterBalance()),
                displayAmount(flow.changeAmount()), flow.txHash(), flow.bizNo(), flow.createTime(),
                flow.updateTime(), flow.userWalletCount());
    }

    private String displayAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return "0";
        }
        BigDecimal amount = new BigDecimal(rawAmount).movePointLeft(PREPAID_DECIMALS).stripTrailingZeros();
        return amount.signum() == 0 ? "0" : amount.toPlainString();
    }

    private String typeName(int type) {
        Map<Integer, String> names = new LinkedHashMap<>();
        names.put(1, "归集");
        names.put(2, "平衡");
        names.put(3, "提现");
        names.put(4, "订阅扣款");
        names.put(5, "合约发布");
        names.put(6, "商户充值");
        names.put(7, "人工调账");
        names.put(8, "KYT检测");
        names.put(9, "商户提现");
        return names.getOrDefault(type, "未知");
    }

    private String actionName(int action) {
        return switch (action) {
            case 1 -> "增加";
            case 2 -> "扣款";
            case 3 -> "退回";
            case 4 -> "补扣";
            default -> "未知";
        };
    }
}
