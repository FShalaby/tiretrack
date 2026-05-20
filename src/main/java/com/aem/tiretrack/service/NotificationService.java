package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aem.tiretrack.dto.notification.NotificationRequest;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.AppNotificationRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class NotificationService {
    private final AppNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(AppNotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<AppNotification> currentNotifications() {
        User user = currentUser();
        return notificationRepository.findTop30ByRecipientUserIdOrRecipientRoleOrderByCreatedAtDesc(user.getId(), user.getRole());
    }

    public AppNotification createForCurrentUser(NotificationRequest request) {
        User user = currentUser();
        AppNotification notification = new AppNotification();
        notification.setRecipientUserId(user.getId());
        notification.setRecipientRole(user.getRole());
        notification.setTitle(blankDefault(request.getTitle(), "Notification"));
        notification.setMessage(blankDefault(request.getMessage(), notification.getTitle()));
        notification.setType(blankDefault(request.getType(), "INFO"));
        notification.setTargetTab(blankDefault(request.getTargetTab(), "Dashboard"));
        return notificationRepository.save(notification);
    }

    public AppNotification markRead(Long id) {
        User user = currentUser();
        AppNotification notification = notificationRepository.findByIdAndRecipientUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllRead() {
        List<AppNotification> notifications = currentNotifications().stream()
                .filter(notification -> !notification.isRead())
                .peek(notification -> notification.setRead(true))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String blankDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
