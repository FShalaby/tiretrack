package com.aem.tiretrack.dto;

import java.time.LocalDateTime;

import com.aem.tiretrack.model.AuditLog;

public class AuditLogResponse {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String message;
    private String performedBy;
    private Long shopId;
    private String shopName;
    private LocalDateTime createdAt;

    public AuditLogResponse(AuditLog auditLog) {
        this.id = auditLog.getId();
        this.action = auditLog.getAction();
        this.entityType = auditLog.getEntityType();
        this.entityId = auditLog.getEntityId();
        this.message = auditLog.getMessage();
        this.performedBy = auditLog.getPerformedBy();
        this.shopId = auditLog.getShop() == null ? null : auditLog.getShop().getId();
        this.shopName = auditLog.getShop() == null ? null : auditLog.getShop().getName();
        this.createdAt = auditLog.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getMessage() { return message; }
    public String getPerformedBy() { return performedBy; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
