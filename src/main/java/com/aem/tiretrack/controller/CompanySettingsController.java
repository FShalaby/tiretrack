package com.aem.tiretrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.CompanySettingsRequest;
import com.aem.tiretrack.dto.CompanySettingsResponse;
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
    public CompanySettingsResponse getSettings(@RequestParam(required = false) Long shopId) {
        return new CompanySettingsResponse(companySettingsService.getSettings(shopId));
    }

    @PutMapping
    public CompanySettingsResponse saveSettings(
            @RequestParam(required = false) Long shopId,
            @Valid @RequestBody CompanySettingsRequest settings) {
        return new CompanySettingsResponse(companySettingsService.saveSettings(shopId, settings));
    }
}
