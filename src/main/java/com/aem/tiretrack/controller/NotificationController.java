package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.notification.AppNotificationResponse;
import com.aem.tiretrack.dto.notification.NotificationRequest;
import com.aem.tiretrack.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<AppNotificationResponse> notifications() {
        return notificationService.currentNotifications().stream().map(AppNotificationResponse::new).toList();
    }

    @PostMapping
    public AppNotificationResponse create(@RequestBody NotificationRequest request) {
        return new AppNotificationResponse(notificationService.createForCurrentUser(request));
    }

    @PutMapping("/{id}/read")
    public AppNotificationResponse markRead(@PathVariable Long id) {
        return new AppNotificationResponse(notificationService.markRead(id));
    }

    @PutMapping("/read-all")
    public void markAllRead() {
        notificationService.markAllRead();
    }
}
