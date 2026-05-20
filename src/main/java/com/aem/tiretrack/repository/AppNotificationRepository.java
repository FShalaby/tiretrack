package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.AppNotification;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findTop30ByRecipientUserIdOrRecipientRoleOrderByCreatedAtDesc(Long recipientUserId, UserRole recipientRole);
    Optional<AppNotification> findByIdAndRecipientUserId(Long id, Long recipientUserId);
}
