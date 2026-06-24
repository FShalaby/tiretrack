package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aem.tiretrack.dto.notification.NotificationRequest;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.exception.ResourceNotFoundException;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.AppNotificationRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class NotificationService {
    private final AppNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ShopContextService shopContextService;

    public NotificationService(AppNotificationRepository notificationRepository, UserRepository userRepository, ShopContextService shopContextService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.shopContextService = shopContextService;
    }

    public List<AppNotification> currentNotifications() {
        User user = currentUser();
        Long shopId = user.getShop() == null ? null : user.getShop().getId();
        return notificationRepository.findVisibleNotifications(user.getId(), user.getRole(), shopId).stream()
                .filter(notification -> canReadNotification(user, notification))
                .limit(30)
                .toList();
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
        notification.setShop(user.getShop());
        return notificationRepository.save(notification);
    }

    public AppNotification markRead(Long id) {
        User user = currentUser();
        AppNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!canReadNotification(user, notification)) {
            throw new ResourceNotFoundException("Notification not found");
        }
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

    private boolean canReadNotification(User user, AppNotification notification) {
        if (notification.getRecipientUserId() != null) {
            return notification.getRecipientUserId().equals(user.getId());
        }

        if (notification.getRecipientRole() == null || notification.getRecipientRole() != user.getRole()) {
            return false;
        }

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return true;
        }

        if (notification.getShop() == null) {
            return user.getShop() == null;
        }

        return user.getShop() != null && notification.getShop().getId().equals(user.getShop().getId());
    }

    private String blankDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
