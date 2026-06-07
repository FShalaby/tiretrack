package com.aem.tiretrack.dto.notification;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.AppNotification;

public class AppNotificationResponse {
    private Long id;
    private Long recipientUserId;
    private UserRole recipientRole;
    private Long shopId;
    private String shopName;
    private String title;
    private String message;
    private String type;
    private String targetTab;
    private boolean read;
    private LocalDateTime createdAt;

    public AppNotificationResponse(AppNotification notification) {
        this.id = notification.getId();
        this.recipientUserId = notification.getRecipientUserId();
        this.recipientRole = notification.getRecipientRole();
        this.shopId = notification.getShop() == null ? null : notification.getShop().getId();
        this.shopName = notification.getShop() == null ? null : notification.getShop().getName();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType();
        this.targetTab = notification.getTargetTab();
        this.read = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getRecipientUserId() { return recipientUserId; }
    public UserRole getRecipientRole() { return recipientRole; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getTargetTab() { return targetTab; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
