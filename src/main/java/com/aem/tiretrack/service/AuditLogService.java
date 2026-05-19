package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        record(action, entityType, entityId, message, getCurrentActor());
    }

    public void record(String action, String entityType, Long entityId, String message, String performedBy) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMessage(message);
        log.setPerformedBy(performedBy);
        auditLogRepository.save(log);
    }

    private String getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "public-booking";
        }

        String name = authentication.getName();
        return "anonymousUser".equals(name) ? "public-booking" : name;
    }
}
