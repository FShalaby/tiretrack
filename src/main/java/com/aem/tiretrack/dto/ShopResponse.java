package com.aem.tiretrack.dto;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.SubscriptionPlan;
import com.aem.tiretrack.model.Shop;

public class ShopResponse {
    private final Long id;
    private final String name;
    private final String legalName;
    private final String phone;
    private final String email;
    private final String address;
    private final Long ownerAdminId;
    private final String ownerAdminName;
    private final String ownerAdminEmail;
    private final SubscriptionPlan subscriptionPlan;
    private final long activeLocationCount;
    private final int locationLimit;
    private final boolean overLocationLimit;
    private final boolean multiLocationAllowed;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ShopResponse(Shop shop) {
        this(shop, 0, 1);
    }

    public ShopResponse(Shop shop, long activeLocationCount, int locationLimit) {
        this.id = shop.getId();
        this.name = shop.getName();
        this.legalName = shop.getLegalName();
        this.phone = shop.getPhone();
        this.email = shop.getEmail();
        this.address = shop.getAddress();
        this.ownerAdminId = shop.getOwnerAdminId();
        this.ownerAdminName = shop.getOwnerAdminName();
        this.ownerAdminEmail = shop.getOwnerAdminEmail();
        this.subscriptionPlan = shop.getSubscriptionPlan();
        this.activeLocationCount = activeLocationCount;
        this.locationLimit = locationLimit;
        this.overLocationLimit = activeLocationCount > locationLimit;
        this.multiLocationAllowed = shop.hasMultiLocationAccess();
        this.active = shop.isActive();
        this.createdAt = shop.getCreatedAt();
        this.updatedAt = shop.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLegalName() { return legalName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public Long getOwnerAdminId() { return ownerAdminId; }
    public String getOwnerAdminName() { return ownerAdminName; }
    public String getOwnerAdminEmail() { return ownerAdminEmail; }
    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public long getActiveLocationCount() { return activeLocationCount; }
    public int getLocationLimit() { return locationLimit; }
    public boolean isOverLocationLimit() { return overLocationLimit; }
    public boolean isMultiLocationAllowed() { return multiLocationAllowed; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
