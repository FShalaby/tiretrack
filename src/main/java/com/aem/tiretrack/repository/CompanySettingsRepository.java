package com.aem.tiretrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.CompanySettings;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
    Optional<CompanySettings> findByShop_Id(Long shopId);
    Optional<CompanySettings> findByShopIsNull();
}
