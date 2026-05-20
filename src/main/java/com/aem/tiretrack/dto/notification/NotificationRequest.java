package com.aem.tiretrack.dto.notification;

public class NotificationRequest {
    private String title;
    private String message;
    private String type;
    private String targetTab;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTargetTab() { return targetTab; }
    public void setTargetTab(String targetTab) { this.targetTab = targetTab; }
}
