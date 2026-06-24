package com.aem.tiretrack.dto;

import com.aem.tiretrack.enums.ShopLocationType;
import com.aem.tiretrack.model.ShopLocation;

public class TireLocationAvailabilityResponse {
    private Long locationId;
    private String locationName;
    private ShopLocationType locationType;
    private boolean customerFacing;
    private int availableQuantity;

    public TireLocationAvailabilityResponse(ShopLocation location, int availableQuantity) {
        this.locationId = location == null ? null : location.getId();
        this.locationName = location == null ? "Unassigned" : location.getName();
        this.locationType = location == null ? null : location.getType();
        this.customerFacing = location != null && location.isCustomerFacing();
        this.availableQuantity = availableQuantity;
    }

    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public ShopLocationType getLocationType() { return locationType; }
    public boolean isCustomerFacing() { return customerFacing; }
    public int getAvailableQuantity() { return availableQuantity; }
}
