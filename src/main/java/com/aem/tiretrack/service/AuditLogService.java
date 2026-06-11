package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aem.tiretrack.model.AuditLog;
import com.aem.tiretrack.repository.AuditLogRepository;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ShopContextService shopContextService;

    public AuditLogService(AuditLogRepository auditLogRepository, ShopContextService shopContextService) {
        this.auditLogRepository = auditLogRepository;
        this.shopContextService = shopContextService;
    }

    public List<AuditLog> latest() {
        if (shopContextService.isSuperAdmin()) {
            return auditLogRepository.findTop25ByOrderByCreatedAtDesc();
        }

        return auditLogRepository.findLatestVisibleForShop(
                shopContextService.requireShopForAdminOrEmployee().getId(),
                PageRequest.of(0, 25));
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
        shopContextService.getCurrentTenantShop().ifPresent(log::setShop);
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
