package com.aem.tiretrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.PlatformLinkOverviewResponse;
import com.aem.tiretrack.dto.PlatformLinkRecordResponse;
import com.aem.tiretrack.dto.PlatformLinkRequest;
import com.aem.tiretrack.service.PlatformLinkService;

@RestController
@RequestMapping("/api/platform/links")
public class PlatformLinkController {
    private final PlatformLinkService platformLinkService;

    public PlatformLinkController(PlatformLinkService platformLinkService) {
        this.platformLinkService = platformLinkService;
    }

    @GetMapping
    public PlatformLinkOverviewResponse getLinkableRecords() {
        return new PlatformLinkOverviewResponse(
                platformLinkService.getAllLinkableRecords(),
                platformLinkService.getAllLocations());
    }

    @PutMapping("/{recordType}/{recordId}")
    public PlatformLinkRecordResponse assignRecord(
            @PathVariable String recordType,
            @PathVariable Long recordId,
            @RequestBody PlatformLinkRequest request) {
        return platformLinkService.assignRecord(recordType, recordId, request);
    }
}
