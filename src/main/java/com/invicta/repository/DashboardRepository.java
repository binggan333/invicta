package com.invicta.repository;

import com.invicta.model.DashboardModels;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public class DashboardRepository {
    private final JdbcClient jdbc;

    public DashboardRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public DashboardModels.Summary summary(long orgId, Integer chainId, LocalDateTime from, LocalDateTime to) {
        DashboardModels.Metrics payments = metrics("order_payment", "payment_id", "settlement_amount", "create_time", "", orgId, chainId, from, to);
        DashboardModels.Metrics recharges = metrics("order_recharge", "id", "recharge_amount", "create_time", "", orgId, chainId, from, to);
        DashboardModels.Metrics withdraws = metrics("order_transfer", "id", "transfer_amount", "creat_time",
                " AND transfer_type=2", orgId, chainId, from, to);
        return new DashboardModels.Summary(payments, recharges, withdraws, from, to);
    }

    private DashboardModels.Metrics metrics(String table, String idColumn, String amountColumn, String timeColumn,
                            String fixedFilter,
                            long orgId, Integer chainId, LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(").append(idColumn)
                .append(") AS total_count, COALESCE(SUM(CAST(").append(amountColumn)
                .append(" AS DECIMAL(36,12))),0) AS total_amount FROM ").append(table)
                .append(" WHERE COALESCE(is_delete,0)=0 AND org_id=:orgId").append(fixedFilter);
        JdbcClient.StatementSpec statement;
        if (chainId != null) sql.append(" AND chain_id=:chainId");
        if (from != null) sql.append(" AND ").append(timeColumn).append(">=:fromTime");
        if (to != null) sql.append(" AND ").append(timeColumn).append("<:toTime");
        statement = jdbc.sql(sql.toString()).param("orgId", orgId);
        if (chainId != null) statement = statement.param("chainId", chainId);
        if (from != null) statement = statement.param("fromTime", from);
        if (to != null) statement = statement.param("toTime", to);
        return statement.query(DashboardModels.Metrics.class).single();
    }

}
