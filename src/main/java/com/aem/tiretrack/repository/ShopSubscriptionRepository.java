package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.ShopSubscription;

public interface ShopSubscriptionRepository extends JpaRepository<ShopSubscription, Long> {
    Optional<ShopSubscription> findByShop_Id(Long shopId);
    List<ShopSubscription> findAllByOrderByUpdatedAtDesc();
}
