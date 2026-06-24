package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.AppNotification;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findTop30ByRecipientUserIdOrRecipientRoleOrderByCreatedAtDesc(Long recipientUserId, UserRole recipientRole);
    List<AppNotification> findTop30ByRecipientUserIdOrRecipientRoleAndShop_IdOrderByCreatedAtDesc(Long recipientUserId, UserRole recipientRole, Long shopId);
    Optional<AppNotification> findByIdAndRecipientUserId(Long id, Long recipientUserId);

    @Query("""
            SELECT notification FROM AppNotification notification
            WHERE notification.recipientUserId = :userId
               OR (notification.recipientRole = :role
                   AND (:shopId IS NULL OR (notification.shop IS NOT NULL AND notification.shop.id = :shopId)))
            ORDER BY notification.createdAt DESC
            """)
    List<AppNotification> findVisibleNotifications(
            @Param("userId") Long userId,
            @Param("role") UserRole role,
            @Param("shopId") Long shopId);
}
