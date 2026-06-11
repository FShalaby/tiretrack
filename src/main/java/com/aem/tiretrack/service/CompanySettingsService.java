package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.CompanySettingsRequest;
import com.aem.tiretrack.model.CompanySettings;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.repository.CompanySettingsRepository;
import com.aem.tiretrack.repository.ShopRepository;

@Service
public class CompanySettingsService {
    private final CompanySettingsRepository companySettingsRepository;
    private final ShopRepository shopRepository;
    private final ShopContextService shopContextService;

    public CompanySettingsService(
            CompanySettingsRepository companySettingsRepository,
            ShopRepository shopRepository,
            ShopContextService shopContextService) {
        this.companySettingsRepository = companySettingsRepository;
        this.shopRepository = shopRepository;
        this.shopContextService = shopContextService;
    }

    @Transactional
    public CompanySettings getSettings() {
        return getSettings(null);
    }

    @Transactional
    public CompanySettings getSettings(Long shopId) {
        Shop shop = resolveSettingsShop(shopId);
        if (shop == null) {
            return getLegacyPlatformSettings();
        }

        List<CompanySettings> existingSettings = companySettingsRepository.findAllByShop_IdOrderByIdAsc(shop.getId());
        if (!existingSettings.isEmpty()) {
            return existingSettings.get(0);
        }

        return companySettingsRepository.save(createSettingsForShop(shop));
    }

    @Transactional
    public CompanySettings saveSettings(CompanySettingsRequest updatedSettings) {
        return saveSettings(null, updatedSettings);
    }

    @Transactional
    public CompanySettings saveSettings(Long shopId, CompanySettingsRequest updatedSettings) {
        CompanySettings settings = getSettings(shopId);

        settings.setShopName(cleanText(updatedSettings.getShopName()));
        settings.setLogoUrl(cleanNullableText(updatedSettings.getLogoUrl()));
        settings.setPhone(cleanNullableText(updatedSettings.getPhone()));
        settings.setAddress(cleanNullableText(updatedSettings.getAddress()));
        settings.setTaxRate(updatedSettings.getTaxRate());
        settings.setInvoiceTerms(cleanNullableText(updatedSettings.getInvoiceTerms()));

        return companySettingsRepository.save(settings);
    }

    private Shop resolveSettingsShop(Long shopId) {
        if (shopContextService.isSuperAdmin()) {
            if (shopId == null) {
                return null;
            }
            return shopRepository.findById(shopId)
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));
        }

        Shop shop = shopContextService.requireShopForAdminOrEmployee();
        if (shopId != null && !shop.getId().equals(shopId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to access this shop settings record.");
        }
        return shop;
    }

    private CompanySettings getLegacyPlatformSettings() {
        List<CompanySettings> existingSettings = companySettingsRepository.findAllByShopIsNullOrderByIdAsc();
        if (!existingSettings.isEmpty()) {
            return existingSettings.get(0);
        }

        return companySettingsRepository.save(new CompanySettings());
    }

    private CompanySettings createSettingsForShop(Shop shop) {
        CompanySettings settings = new CompanySettings();
        settings.setShop(shop);
        settings.setShopName(shop.getName());

        companySettingsRepository.findAllByShopIsNullOrderByIdAsc().stream().findFirst().ifPresent(legacy -> {
            settings.setLogoUrl(legacy.getLogoUrl());
            settings.setPhone(legacy.getPhone());
            settings.setAddress(legacy.getAddress());
            settings.setTaxRate(legacy.getTaxRate());
            settings.setInvoiceTerms(legacy.getInvoiceTerms());
        });

        return settings;
    }

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanNullableText(String value) {
        String cleaned = cleanText(value);
        return cleaned == null || cleaned.isBlank() ? null : cleaned;
    }
}
