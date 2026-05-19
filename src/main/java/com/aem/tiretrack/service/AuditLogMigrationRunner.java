package com.aem.tiretrack.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.repository.AuditLogRepository;

@Component
public class AuditLogMigrationRunner implements ApplicationRunner {
    private final AuditLogRepository auditLogRepository;

    public AuditLogMigrationRunner(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        auditLogRepository.relabelLegacySystemActors();
    }
}
