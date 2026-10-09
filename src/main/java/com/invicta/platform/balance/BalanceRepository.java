package com.invicta.platform.balance;

import com.invicta.platform.common.PageQuery;
import com.invicta.platform.common.PageResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BalanceRepository {
    private final JdbcClient jdbc;
    public BalanceRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public PageResponse<BalanceView> list(long orgId, Integer chainId, Integer status, String keyword,
                                          LocalDateTime from, LocalDateTime to, PageQuery page) {
        Filter filter = filter(orgId, chainId, status, keyword, from, to);
        long total = query("SELECT COUNT(*) FROM order_balance" + filter.where, filter.params).query(Long.class).single();
        filter.params.put("limit", page.size()); filter.params.put("offset", page.offset());
        List<BalanceView> items = query("""
                SELECT id, org_id, chain_id, chain_name, wallet_id, wallet_addr_hot, wallet_addr_cold,
                       status, balance_hash, create_time, wallet_addr_manager, manager_id, balance_type, balance_id
                FROM order_balance
                """ + filter.where + " ORDER BY create_time DESC LIMIT :limit OFFSET :offset", filter.params)
                .query(BalanceView.class).list();
        return PageResponse.of(items, total, page);
    }

    @Transactional(readOnly = true)
    public Optional<BalanceDetail> detail(long orgId, long id) {
        Optional<BalanceView> order = jdbc.sql("""
                SELECT id, org_id, chain_id, chain_name, wallet_id, wallet_addr_hot, wallet_addr_cold,
                       status, balance_hash, create_time, wallet_addr_manager, manager_id, balance_type, balance_id
                FROM order_balance WHERE id=:id AND org_id=:orgId AND COALESCE(is_delete,0)=0
                """).param("id", id).param("orgId", orgId).query(BalanceView.class).optional();
        return order.map(value -> new BalanceDetail(value, jdbc.sql("""
                SELECT id, balance_id, currency_id, currency_address, logindex, commission_total,
                       commission_agent, commission_platform, amount, create_time
                FROM order_balance_detail WHERE balance_id=:balanceId ORDER BY id
                """).param("balanceId", value.balanceId()).query(BalanceLine.class).list()));
    }

    @Transactional(readOnly = true)
    public Statistics statistics(long orgId, Integer chainId, Integer status, String keyword,
                                 LocalDateTime from, LocalDateTime to) {
        Filter filter = filter(orgId, chainId, status, keyword, from, to);
        return query("""
                SELECT COUNT(DISTINCT b.id) AS total_count,
                       COALESCE(SUM(CAST(d.amount AS DECIMAL(36,12))),0) AS total_amount,
                       COALESCE(SUM(CAST(d.commission_total AS DECIMAL(36,12))),0) AS total_commission
                FROM (SELECT id, balance_id FROM order_balance
                """ + filter.where + ") b LEFT JOIN order_balance_detail d ON d.balance_id=b.balance_id",
                filter.params).query(Statistics.class).single();
    }

    @Transactional(readOnly = true)
    public Percentage percentage(long orgId, int chainId, String walletType) {
        BigDecimal all = walletBalance(orgId, chainId, null);
        BigDecimal selected = walletBalance(orgId, chainId, walletType);
        BigDecimal percent = all.signum() == 0 ? BigDecimal.ZERO
                : selected.multiply(BigDecimal.valueOf(100)).divide(all, 2, java.math.RoundingMode.HALF_UP);
        return new Percentage(selected, all, percent);
    }

    private BigDecimal walletBalance(long orgId, int chainId, String walletType) {
        String sql = "SELECT COALESCE(SUM(CAST(balance AS DECIMAL(36,12))),0) FROM sys_org_chain_walet_currency_balance "
                + "WHERE COALESCE(is_delete,0)=0 AND org_id=:orgId AND chain_id=:chainId"
                + (walletType == null ? "" : " AND wallet_type=:walletType");
        JdbcClient.StatementSpec statement = jdbc.sql(sql).param("orgId", orgId).param("chainId", chainId);
        if (walletType != null) statement = statement.param("walletType", walletType);
        return statement.query(BigDecimal.class).single();
    }

    private Filter filter(long orgId, Integer chainId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        StringBuilder where = new StringBuilder(" WHERE COALESCE(is_delete,0)=0 AND org_id=:orgId");
        Map<String, Object> params = new LinkedHashMap<>(); params.put("orgId", orgId);
        if (chainId != null) { where.append(" AND chain_id=:chainId"); params.put("chainId", chainId); }
        if (status != null) { where.append(" AND status=:status"); params.put("status", status); }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (balance_hash LIKE :keyword OR balance_id LIKE :keyword OR wallet_addr_hot LIKE :keyword OR wallet_addr_cold LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        if (from != null) { where.append(" AND create_time>=:fromTime"); params.put("fromTime", from); }
        if (to != null) { where.append(" AND create_time<:toTime"); params.put("toTime", to); }
        return new Filter(where.toString(), params);
    }

    private JdbcClient.StatementSpec query(String sql, Map<String, Object> params) {
        JdbcClient.StatementSpec statement = jdbc.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) statement = statement.param(entry.getKey(), entry.getValue());
        return statement;
    }

    private record Filter(String where, Map<String, Object> params) {}
    public record BalanceView(long id, long orgId, int chainId, String chainName, Long walletId,
            String walletAddrHot, String walletAddrCold, int status, String balanceHash, LocalDateTime createTime,
            String walletAddrManager, Long managerId, Integer balanceType, String balanceId) {}
    public record BalanceLine(long id, String balanceId, int currencyId, String currencyAddress, Integer logindex,
            BigDecimal commissionTotal, BigDecimal commissionAgent, BigDecimal commissionPlatform,
            BigDecimal amount, LocalDateTime createTime) {}
    public record BalanceDetail(BalanceView order, List<BalanceLine> lines) {}
    public record Statistics(long totalCount, BigDecimal totalAmount, BigDecimal totalCommission) {}
    public record Percentage(BigDecimal selectedBalance, BigDecimal totalBalance, BigDecimal percentage) {}
}
