package com.aem.tiretrack.dto.booking;

import com.aem.tiretrack.enums.ShopLocationType;
import com.aem.tiretrack.model.ShopLocation;

public class PublicShopLocationResponse {
    private final Long shopId;
    private final String shopName;
    private final Long locationId;
    private final String locationName;
    private final ShopLocationType type;
    private final String address;
    private final String city;
    private final String province;
    private final String postalCode;
    private final boolean active;
    private final boolean customerFacing;

    public PublicShopLocationResponse(ShopLocation location) {
        this.shopId = location.getShop() == null ? null : location.getShop().getId();
        this.shopName = location.getShop() == null ? null : location.getShop().getName();
        this.locationId = location.getId();
        this.locationName = location.getName();
        this.type = location.getType();
        this.address = location.getAddress();
        this.city = location.getCity();
        this.province = location.getProvince();
        this.postalCode = location.getPostalCode();
        this.active = location.isActive();
        this.customerFacing = location.isCustomerFacing();
    }

    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public ShopLocationType getType() { return type; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public boolean isActive() { return active; }
    public boolean isCustomerFacing() { return customerFacing; }
}
