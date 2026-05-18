package com.aem.tiretrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.CompanySettings;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
}
