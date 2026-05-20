package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.notification.NotificationRequest;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<AppNotification> notifications() {
        return notificationService.currentNotifications();
    }

    @PostMapping
    public AppNotification create(@RequestBody NotificationRequest request) {
        return notificationService.createForCurrentUser(request);
    }

    @PutMapping("/{id}/read")
    public AppNotification markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }

    @PutMapping("/read-all")
    public void markAllRead() {
        notificationService.markAllRead();
    }
}
