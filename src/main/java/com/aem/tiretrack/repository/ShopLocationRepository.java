package com.aem.tiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.ShopLocation;

public interface ShopLocationRepository extends JpaRepository<ShopLocation, Long> {
    List<ShopLocation> findByShop_Id(Long shopId);
    List<ShopLocation> findByShop_IdAndActiveTrue(Long shopId);
    List<ShopLocation> findByShop_IdAndActiveTrueAndCustomerFacingTrue(Long shopId);
    long countByShop_IdAndActiveTrue(Long shopId);
}
