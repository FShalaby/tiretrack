package com.aem.tiretrack.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.model.CompanySettings;
import com.aem.tiretrack.repository.CompanySettingsRepository;

@Service
public class CompanySettingsService {
    private static final Long SETTINGS_ID = 1L;

    private final CompanySettingsRepository companySettingsRepository;

    public CompanySettingsService(CompanySettingsRepository companySettingsRepository) {
        this.companySettingsRepository = companySettingsRepository;
    }

    public CompanySettings getSettings() {
        return companySettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> companySettingsRepository.save(new CompanySettings()));
    }

    @Transactional
    public CompanySettings saveSettings(CompanySettings updatedSettings) {
        CompanySettings settings = getSettings();

        settings.setShopName(updatedSettings.getShopName());
        settings.setLogoUrl(updatedSettings.getLogoUrl());
        settings.setPhone(updatedSettings.getPhone());
        settings.setAddress(updatedSettings.getAddress());
        settings.setTaxRate(updatedSettings.getTaxRate());
        settings.setInvoiceTerms(updatedSettings.getInvoiceTerms());

        return companySettingsRepository.save(settings);
    }
}
