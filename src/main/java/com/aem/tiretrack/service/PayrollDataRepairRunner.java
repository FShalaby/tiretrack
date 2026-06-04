package com.aem.tiretrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = {"app.backfill.enabled", "app.backfill.payroll-repair.enabled"}, havingValue = "true")
@Order(0)
public class PayrollDataRepairRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PayrollDataRepairRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public PayrollDataRepairRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int repairedRows = jdbcTemplate.update(
                    "UPDATE payroll_records SET accounting_synced = false WHERE accounting_synced IS NULL");
            if (repairedRows > 0) {
                log.info("Repaired {} payroll records with null accounting_synced values.", repairedRows);
            }
        } catch (RuntimeException ex) {
            log.warn("Skipped payroll accounting_synced repair: {}", ex.getMessage());
        }
    }
}
