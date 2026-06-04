package com.aem.tiretrack.dto;

import java.util.List;

public class PlatformLinkOverviewResponse {
    private final List<PlatformLinkRecordResponse> records;
    private final List<ShopLocationResponse> locations;

    public PlatformLinkOverviewResponse(List<PlatformLinkRecordResponse> records, List<ShopLocationResponse> locations) {
        this.records = records;
        this.locations = locations;
    }

    public List<PlatformLinkRecordResponse> getRecords() {
        return records;
    }

    public List<ShopLocationResponse> getLocations() {
        return locations;
    }
}
