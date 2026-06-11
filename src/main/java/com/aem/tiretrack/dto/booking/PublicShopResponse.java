package com.aem.tiretrack.dto.booking;

import com.aem.tiretrack.model.Shop;

public class PublicShopResponse {
    private final Long shopId;
    private final String shopName;
    private final boolean active;

    public PublicShopResponse(Shop shop) {
        this.shopId = shop.getId();
        this.shopName = shop.getName();
        this.active = shop.isActive();
    }

    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public boolean isActive() { return active; }
}
