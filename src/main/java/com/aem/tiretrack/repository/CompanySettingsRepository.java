package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.CompanySettings;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
    Optional<CompanySettings> findByShop_Id(Long shopId);
    List<CompanySettings> findAllByShop_IdOrderByIdAsc(Long shopId);
    Optional<CompanySettings> findByShopIsNull();
    List<CompanySettings> findAllByShopIsNullOrderByIdAsc();
}
