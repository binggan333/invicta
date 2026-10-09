package com.invicta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DashboardModels {
    private DashboardModels() {}

    public record Summary(Metrics payments, Metrics recharges, Metrics withdraws,
                          LocalDateTime from, LocalDateTime to) {}
    public record Metrics(long totalCount, BigDecimal totalAmount) {}
}
