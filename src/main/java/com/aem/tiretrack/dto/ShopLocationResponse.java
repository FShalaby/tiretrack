package com.aem.tiretrack.dto;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.ShopLocationType;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;

public class ShopLocationResponse {
    private final Long id;
    private final Long shopId;
    private final String shopName;
    private final String name;
    private final ShopLocationType type;
    private final String address;
    private final String city;
    private final String province;
    private final String postalCode;
    private final String phone;
    private final String email;
    private final boolean customerFacing;
    private final boolean active;
    private final boolean overBasicLimit;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ShopLocationResponse(ShopLocation location) {
        Shop shop = location.getShop();
        this.id = location.getId();
        this.shopId = shop == null ? null : shop.getId();
        this.shopName = shop == null ? null : shop.getName();
        this.name = location.getName();
        this.type = location.getType();
        this.address = location.getAddress();
        this.city = location.getCity();
        this.province = location.getProvince();
        this.postalCode = location.getPostalCode();
        this.phone = location.getPhone();
        this.email = location.getEmail();
        this.customerFacing = location.isCustomerFacing();
        this.active = location.isActive();
        this.overBasicLimit = shop != null && !shop.hasMultiLocationAccess();
        this.createdAt = location.getCreatedAt();
        this.updatedAt = location.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getName() { return name; }
    public ShopLocationType getType() { return type; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public boolean isCustomerFacing() { return customerFacing; }
    public boolean isActive() { return active; }
    public boolean isOverBasicLimit() { return overBasicLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
