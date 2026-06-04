package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;

public class TireResponse {
    private final Long id;
    private final String brand;
    private final String model;
    private final int width;
    private final int aspectRatio;
    private final int rimSize;
    private final String tireSize;
    private final String season;
    private final Condition condition;
    private final int quantity;
    private final int reservedQuantity;
    private final int availableQuantity;
    private final BigDecimal price;
    private final String location;
    private final Long shopId;
    private final String shopName;
    private final Long locationId;
    private final String locationName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TireResponse(Tire tire) {
        Shop shop = tire.getShop();
        ShopLocation shopLocation = tire.getShopLocation();

        this.id = tire.getId();
        this.brand = tire.getBrand();
        this.model = tire.getModel();
        this.width = tire.getWidth();
        this.aspectRatio = tire.getAspectRatio();
        this.rimSize = tire.getRimSize();
        this.tireSize = tire.getTireSize();
        this.season = tire.getSeason();
        this.condition = tire.getCondition();
        this.quantity = tire.getQuantity();
        this.reservedQuantity = tire.getReservedQuantity();
        this.availableQuantity = tire.getAvailableQuantity();
        this.price = tire.getPrice();
        this.location = tire.getLocation();
        this.shopId = shop == null ? null : shop.getId();
        this.shopName = shop == null ? null : shop.getName();
        this.locationId = shopLocation == null ? null : shopLocation.getId();
        this.locationName = shopLocation == null ? null : shopLocation.getName();
        this.createdAt = tire.getCreatedAt();
        this.updatedAt = tire.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getWidth() { return width; }
    public int getAspectRatio() { return aspectRatio; }
    public int getRimSize() { return rimSize; }
    public String getTireSize() { return tireSize; }
    public String getSeason() { return season; }
    public Condition getCondition() { return condition; }
    public int getQuantity() { return quantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public BigDecimal getPrice() { return price; }
    public String getLocation() { return location; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
