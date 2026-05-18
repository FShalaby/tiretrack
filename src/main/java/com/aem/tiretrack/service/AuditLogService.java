package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aem.tiretrack.model.AuditLog;
import com.aem.tiretrack.repository.AuditLogRepository;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> latest() {
        return auditLogRepository.findTop25ByOrderByCreatedAtDesc();
    }

    public void record(String action, String entityType, Long entityId, String message) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMessage(message);
        auditLogRepository.save(log);
    }
}
