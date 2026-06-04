package com.aem.tiretrack.dto;

public class PlatformLinkRecordResponse {
    private final String type;
    private final Long id;
    private final String label;
    private final String detail;
    private final String status;
    private final Long shopId;
    private final String shopName;
    private final Long locationId;
    private final String locationName;

    public PlatformLinkRecordResponse(
            String type,
            Long id,
            String label,
            String detail,
            String status,
            Long shopId,
            String shopName,
            Long locationId,
            String locationName) {
        this.type = type;
        this.id = id;
        this.label = label;
        this.detail = detail;
        this.status = status;
        this.shopId = shopId;
        this.shopName = shopName;
        this.locationId = locationId;
        this.locationName = locationName;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDetail() {
        return detail;
    }

    public String getStatus() {
        return status;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }
}
