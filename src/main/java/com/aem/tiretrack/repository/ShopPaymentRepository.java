package com.aem.tiretrack.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.enums.ShopPaymentStatus;
import com.aem.tiretrack.model.ShopPayment;

public interface ShopPaymentRepository extends JpaRepository<ShopPayment, Long> {
    List<ShopPayment> findByShop_IdOrderByCreatedAtDesc(Long shopId);
    List<ShopPayment> findTop25ByOrderByCreatedAtDesc();
    long countByPaymentStatus(ShopPaymentStatus status);
    List<ShopPayment> findByPaymentStatusAndPaidAtBetween(ShopPaymentStatus status, LocalDateTime start, LocalDateTime end);
}
