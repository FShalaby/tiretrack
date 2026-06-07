package com.aem.tiretrack.dto.customer;

import java.time.LocalDateTime;

import com.aem.tiretrack.model.CustomerNotification;

public class CustomerNotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;

    public CustomerNotificationResponse(CustomerNotification notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType();
        this.read = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
