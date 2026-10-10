package com.invicta.repository;

import com.invicta.model.OrganizationAccount;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class OrganizationAccountRepository {
    private final JdbcClient jdbc;

    public OrganizationAccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Optional<OrganizationAccount> findActiveByEmail(String email) {
        List<AccountRow> accounts = jdbc.sql("""
                        SELECT org_id, org_name, org_email, org_type
                        FROM sys_organ
                        WHERE LOWER(org_email) = LOWER(:email)
                          AND org_state = 0
                          AND COALESCE(is_delete, 0) = 0
                        LIMIT 1
                        """)
                .param("email", email.trim())
                .query(AccountRow.class)
                .list();
        if (accounts.isEmpty()) return Optional.empty();

        AccountRow row = accounts.get(0);
        return Optional.of(new OrganizationAccount(row.orgId(), row.orgName(), row.orgEmail(), row.orgType(),
                Set.of(roleFor(row.orgType()))));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return jdbc.sql("""
                        SELECT COUNT(*) FROM sys_organ
                        WHERE LOWER(org_email) = LOWER(:email) AND COALESCE(is_delete, 0) = 0
                        """)
                .param("email", email.trim())
                .query(Long.class)
                .single() > 0;
    }

    private String roleFor(int orgType) {
        return switch (orgType) {
            case 0 -> "operator";
            case 1, 2 -> "agent";
            case 3 -> "proMerchant";
            case 4 -> "liteMerchant";
            case 5 -> "stdMerchant";
            default -> "organization";
        };
    }

    private record AccountRow(long orgId, String orgName, String orgEmail, int orgType) {}
}
