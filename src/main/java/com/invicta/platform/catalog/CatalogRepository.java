package com.invicta.platform.catalog;

import com.invicta.platform.common.PageQuery;
import com.invicta.platform.common.PageResponse;
import com.invicta.platform.security.CurrentUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CatalogRepository {
    private final JdbcClient jdbc;

    public CatalogRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrganizationView> organizations(CurrentUser user, String keyword, Integer type, Integer state, PageQuery page) {
        StringBuilder where = new StringBuilder(" WHERE COALESCE(is_delete,0)=0");
        Map<String, Object> params = new LinkedHashMap<>();
        if (!user.operator()) {
            if (user.agent()) where.append(" AND (org_id=:scopeOrgId OR org_parent_id=:scopeOrgId)");
            else where.append(" AND org_id=:scopeOrgId");
            params.put("scopeOrgId", user.orgId());
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (org_name LIKE :keyword OR org_email LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        if (type != null) { where.append(" AND org_type=:type"); params.put("type", type); }
        if (state != null) { where.append(" AND org_state=:state"); params.put("state", state); }

        long total = query("SELECT COUNT(*) FROM sys_organ" + where, params).query(Long.class).single();
        params.put("limit", page.size());
        params.put("offset", page.offset());
        List<OrganizationView> items = query("""
                SELECT org_id, org_name, org_email, org_phone, org_type, org_parent_id, org_state, create_time
                FROM sys_organ
                """ + where + " ORDER BY org_id DESC LIMIT :limit OFFSET :offset", params)
                .query(OrganizationView.class).list();
        return PageResponse.of(items, total, page);
    }

    @Transactional(readOnly = true)
    public List<ChainView> chains() {
        return jdbc.sql("""
                SELECT chain_id, chain_name, chain_symbol, out_id, block_explorer, chain_type, is_default
                FROM sys_chain WHERE COALESCE(is_delete,0)=0 ORDER BY o_sort, chain_id
                """).query(ChainView.class).list();
    }

    @Transactional(readOnly = true)
    public List<CurrencyView> currencies(Integer chainId) {
        String sql = """
                SELECT currency_id, chain_id, name, symbol, decimals, min_withdraw_amount, fee, address, min_valid_amount
                FROM sys_currency WHERE COALESCE(is_delete,0)=0
                """ + (chainId == null ? "" : " AND chain_id=:chainId") + " ORDER BY o_sort, currency_id";
        JdbcClient.StatementSpec statement = jdbc.sql(sql);
        if (chainId != null) statement = statement.param("chainId", chainId);
        return statement.query(CurrencyView.class).list();
    }

    private JdbcClient.StatementSpec query(String sql, Map<String, Object> params) {
        JdbcClient.StatementSpec statement = jdbc.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) statement = statement.param(entry.getKey(), entry.getValue());
        return statement;
    }

    public record OrganizationView(long orgId, String orgName, String orgEmail, String orgPhone,
                                   int orgType, Long orgParentId, int orgState, LocalDateTime createTime) {}
    public record ChainView(int chainId, String chainName, String chainSymbol, String outId,
                            String blockExplorer, int chainType, Boolean isDefault) {}
    public record CurrencyView(int currencyId, int chainId, String name, String symbol, int decimals,
                               String minWithdrawAmount, String fee, String address, String minValidAmount) {}
}
