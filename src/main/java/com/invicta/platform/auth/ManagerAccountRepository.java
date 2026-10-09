package com.invicta.platform.auth;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class ManagerAccountRepository {
    private final JdbcClient jdbc;

    public ManagerAccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Optional<ManagerAccount> findActiveByEmail(String email) {
        List<AccountRow> accounts = jdbc.sql("""
                        SELECT manager_id, manager_name, manager_email, manager_type, org_id
                        FROM sys_manager
                        WHERE LOWER(manager_email) = LOWER(:email)
                          AND manager_state IN (0, 3)
                          AND COALESCE(is_delete, 0) = 0
                        LIMIT 1
                        """)
                .param("email", email.trim())
                .query(AccountRow.class)
                .list();
        if (accounts.isEmpty()) return Optional.empty();

        AccountRow row = accounts.getFirst();
        LinkedHashSet<String> roles = new LinkedHashSet<>(jdbc.sql("""
                        SELECT r.role_name
                        FROM sys_user_role ur
                        JOIN sys_role r ON r.role_id = ur.role_id
                        WHERE ur.user_id = :managerId
                        """)
                .param("managerId", row.managerId())
                .query(String.class)
                .list());
        return Optional.of(new ManagerAccount(row.managerId(), row.managerName(), row.managerEmail(),
                row.managerType(), row.orgId(), roles));
    }

    private record AccountRow(long managerId, String managerName, String managerEmail, int managerType, long orgId) {}
}
