package com.aem.tiretrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.model.CompanySettings;
import com.aem.tiretrack.service.CompanySettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
public class CompanySettingsController {
    private final CompanySettingsService companySettingsService;

    public CompanySettingsController(CompanySettingsService companySettingsService) {
        this.companySettingsService = companySettingsService;
    }

    @GetMapping
    public CompanySettings getSettings() {
        return companySettingsService.getSettings();
    }

    @PutMapping
    public CompanySettings saveSettings(@Valid @RequestBody CompanySettings settings) {
        return companySettingsService.saveSettings(settings);
    }
}
