package com.invicta.repository;

import com.invicta.common.PageQuery;
import com.invicta.common.PageResponse;
import com.invicta.model.SummaryModels;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SummaryRepository {
    private final JdbcClient jdbc;

    public SummaryRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public PageResponse<SummaryModels.View> list(long orgId, Integer chainId, Integer status, String keyword,
                                               LocalDateTime from, LocalDateTime to, PageQuery page) {
        Filter filter = filter(orgId, chainId, status, keyword, from, to);
        long total = query("SELECT COUNT(*) FROM order_summary" + filter.where, filter.params).query(Long.class).single();
        filter.params.put("limit", page.size()); filter.params.put("offset", page.offset());
        List<SummaryModels.View> items = query("""
                SELECT id, org_id, chain_id, chain_name, wallet_id, wallet_addr_hot, wallet_addr_cold,
                       status, summary_hash, create_time, wallet_addr_manager, manager_id,
                       user_wallet_ids, summary_type, summary_id
                FROM order_summary
                """ + filter.where + " ORDER BY create_time DESC LIMIT :limit OFFSET :offset", filter.params)
                .query(SummaryModels.View.class).list();
        return PageResponse.of(items, total, page);
    }

    @Transactional(readOnly = true)
    public Optional<SummaryModels.Detail> detail(long orgId, long id) {
        Optional<SummaryModels.View> order = jdbc.sql("""
                SELECT id, org_id, chain_id, chain_name, wallet_id, wallet_addr_hot, wallet_addr_cold,
                       status, summary_hash, create_time, wallet_addr_manager, manager_id,
                       user_wallet_ids, summary_type, summary_id
                FROM order_summary WHERE id=:id AND org_id=:orgId AND COALESCE(is_delete,0)=0
                """).param("id", id).param("orgId", orgId).query(SummaryModels.View.class).optional();
        return order.map(value -> new SummaryModels.Detail(value, jdbc.sql("""
                        SELECT id, summary_id, user_wallet_id, user_wallet_address, cold_address,
                               currency_id, currency_address, amount, logindex, create_time
                        FROM order_summary_detail WHERE summary_id=:summaryId ORDER BY id
                        """).param("summaryId", value.summaryId()).query(SummaryModels.Line.class).list()));
    }

    @Transactional(readOnly = true)
    public SummaryModels.Statistics statistics(long orgId, Integer chainId, Integer status, String keyword,
                                 LocalDateTime from, LocalDateTime to) {
        Filter filter = filter(orgId, chainId, status, keyword, from, to);
        return query("""
                SELECT COUNT(DISTINCT s.id) AS total_count,
                       COALESCE(SUM(CAST(d.amount AS DECIMAL(36,12))),0) AS total_amount
                FROM (SELECT id, summary_id FROM order_summary
                """ + filter.where + ") s LEFT JOIN order_summary_detail d ON d.summary_id=s.summary_id", filter.params)
                .query(SummaryModels.Statistics.class).single();
    }

    @Transactional(readOnly = true)
    public List<SummaryModels.WalletCurrencyBalance> walletBalances(long orgId, long walletId) {
        return jdbc.sql("""
                SELECT b.balance_id, b.chain_id, b.wallet_id, b.currency_id, c.symbol,
                       b.balance, b.update_time
                FROM org_user_chain_walet_currency_balance b
                JOIN org_user_chain_wallet w ON w.wallet_id=b.wallet_id
                JOIN org_user u ON u.user_id=w.user_id
                LEFT JOIN sys_currency c ON c.currency_id=b.currency_id
                WHERE b.wallet_id=:walletId AND u.org_id=:orgId AND COALESCE(b.is_delete,0)=0
                ORDER BY b.currency_id
                """).param("walletId", walletId).param("orgId", orgId).query(SummaryModels.WalletCurrencyBalance.class).list();
    }

    private Filter filter(long orgId, Integer chainId, Integer status, String keyword, LocalDateTime from, LocalDateTime to) {
        StringBuilder where = new StringBuilder(" WHERE COALESCE(is_delete,0)=0 AND org_id=:orgId");
        Map<String, Object> params = new LinkedHashMap<>(); params.put("orgId", orgId);
        if (chainId != null) { where.append(" AND chain_id=:chainId"); params.put("chainId", chainId); }
        if (status != null) { where.append(" AND status=:status"); params.put("status", status); }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (summary_hash LIKE :keyword OR summary_id LIKE :keyword OR FIND_IN_SET(:exactKeyword,user_wallet_ids))");
            params.put("keyword", "%" + keyword.trim() + "%"); params.put("exactKeyword", keyword.trim());
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
}
