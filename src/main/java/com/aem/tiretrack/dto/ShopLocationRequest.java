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
    private String city;
    private String province;
    private String postalCode;
    private String phone;
    private String email;
    private Boolean customerFacing;
    private Boolean active;

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ShopLocationType getType() { return type; }
    public void setType(ShopLocationType type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getCustomerFacing() { return customerFacing; }
    public void setCustomerFacing(Boolean customerFacing) { this.customerFacing = customerFacing; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
