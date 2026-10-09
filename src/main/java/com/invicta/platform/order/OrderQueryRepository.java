package com.invicta.platform.order;

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

@Repository
public class OrderQueryRepository {
    private final JdbcClient jdbc;

    public OrderQueryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentView> payments(long orgId, Integer chainId, Integer currencyId, Integer status,
                                               String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        QueryParts parts = filters("org_id", orgId, chainId, currencyId, status, "payment_status", keyword,
                List.of("out_trade_no", "out_payment_no", "transfer_hash", "user_wallet_address"), "create_time", from, to);
        long total = count("order_payment", parts);
        parts.params.put("limit", page.size()); parts.params.put("offset", page.offset());
        List<PaymentView> items = query("""
                SELECT payment_id, org_id, chain_id, currency_id, out_trade_no, out_payment_no,
                       quote_amount, settlement_amount, expected_amount, payment_status, transfer_hash,
                       user_wallet_address, user_email, create_time
                FROM order_payment
                """ + parts.where + " ORDER BY payment_id DESC LIMIT :limit OFFSET :offset", parts.params)
                .query(PaymentView.class).list();
        return PageResponse.of(items, total, page);
    }

    @Transactional(readOnly = true)
    public PageResponse<RechargeView> recharges(long orgId, Integer chainId, Integer currencyId, Integer status,
                                                 String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        QueryParts parts = filters("org_id", orgId, chainId, currencyId, status, "recharge_status", keyword,
                List.of("transfer_hash", "from_address"), "create_time", from, to);
        long total = count("order_recharge", parts);
        parts.params.put("limit", page.size()); parts.params.put("offset", page.offset());
        List<RechargeView> items = query("""
                SELECT id, org_id, chain_id, wallet_id, currency_id, recharge_status, recharge_amount,
                       transfer_hash, from_address, block_time, create_time
                FROM order_recharge
                """ + parts.where + " ORDER BY id DESC LIMIT :limit OFFSET :offset", parts.params)
                .query(RechargeView.class).list();
        return PageResponse.of(items, total, page);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransferView> transfers(long orgId, Integer chainId, Integer currencyId, Integer status,
                                                 String keyword, LocalDateTime from, LocalDateTime to, PageQuery page) {
        QueryParts parts = filters("org_id", orgId, chainId, currencyId, status, "transfer_status", keyword,
                List.of("out_trade_no", "transfer_hash", "transfer_address"), "creat_time", from, to);
        long total = count("order_transfer", parts);
        parts.params.put("limit", page.size()); parts.params.put("offset", page.offset());
        List<TransferView> items = query("""
                SELECT id, org_id, chain_id, currency_id, out_trade_no, transfer_type, transfer_status,
                       transfer_amount, transfer_address, total_amount, transfer_hash, transfer_fee, creat_time
                FROM order_transfer
                """ + parts.where + " ORDER BY id DESC LIMIT :limit OFFSET :offset", parts.params)
                .query(TransferView.class).list();
        return PageResponse.of(items, total, page);
    }

    private QueryParts filters(String orgColumn, long orgId, Integer chainId, Integer currencyId, Integer status,
                               String statusColumn, String keyword, List<String> keywordColumns, String timeColumn,
                               LocalDateTime from, LocalDateTime to) {
        StringBuilder where = new StringBuilder(" WHERE COALESCE(is_delete,0)=0 AND ").append(orgColumn).append("=:orgId");
        Map<String, Object> params = new LinkedHashMap<>(); params.put("orgId", orgId);
        if (chainId != null) { where.append(" AND chain_id=:chainId"); params.put("chainId", chainId); }
        if (currencyId != null) { where.append(" AND currency_id=:currencyId"); params.put("currencyId", currencyId); }
        if (status != null) { where.append(" AND ").append(statusColumn).append("=:status"); params.put("status", status); }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (");
            for (int i = 0; i < keywordColumns.size(); i++) {
                if (i > 0) where.append(" OR ");
                where.append(keywordColumns.get(i)).append(" LIKE :keyword");
            }
            where.append(')'); params.put("keyword", "%" + keyword.trim() + "%");
        }
        if (from != null) { where.append(" AND ").append(timeColumn).append(">=:fromTime"); params.put("fromTime", from); }
        if (to != null) { where.append(" AND ").append(timeColumn).append("<:toTime"); params.put("toTime", to); }
        return new QueryParts(where.toString(), params);
    }

    private long count(String table, QueryParts parts) {
        return query("SELECT COUNT(*) FROM " + table + parts.where, parts.params).query(Long.class).single();
    }

    private JdbcClient.StatementSpec query(String sql, Map<String, Object> params) {
        JdbcClient.StatementSpec statement = jdbc.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) statement = statement.param(entry.getKey(), entry.getValue());
        return statement;
    }

    private record QueryParts(String where, Map<String, Object> params) {}
    public record PaymentView(long paymentId, long orgId, int chainId, int currencyId, String outTradeNo,
                              String outPaymentNo, BigDecimal quoteAmount, BigDecimal settlementAmount,
                              BigDecimal expectedAmount, int paymentStatus, String transferHash,
                              String userWalletAddress, String userEmail, LocalDateTime createTime) {}
    public record RechargeView(long id, long orgId, int chainId, Long walletId, int currencyId, int rechargeStatus,
                               BigDecimal rechargeAmount, String transferHash, String fromAddress,
                               String blockTime, LocalDateTime createTime) {}
    public record TransferView(long id, long orgId, int chainId, int currencyId, String outTradeNo, int transferType,
                               int transferStatus, BigDecimal transferAmount, String transferAddress,
                               BigDecimal totalAmount, String transferHash, BigDecimal transferFee,
                               LocalDateTime creatTime) {}
}
