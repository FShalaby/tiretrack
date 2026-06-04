package com.aem.tiretrack.dto;

import com.aem.tiretrack.enums.ShopLocationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShopLocationRequest {
    @NotNull(message = "Shop is required")
    private Long shopId;

    @NotBlank(message = "Location name is required")
    private String name;

    @NotNull(message = "Location type is required")
    private ShopLocationType type;

    private String address;
    private Boolean active;

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ShopLocationType getType() { return type; }
    public void setType(ShopLocationType type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
