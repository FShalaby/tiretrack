package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.User;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {
    List<CustomerNotification> findByCustomerOrderByCreatedAtDesc(User customer);
    Optional<CustomerNotification> findByIdAndCustomer(Long id, User customer);
}
