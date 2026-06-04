package com.aem.tiretrack.dto;

import com.aem.tiretrack.enums.SubscriptionPlan;

import jakarta.validation.constraints.NotBlank;

public class ShopRequest {
    @NotBlank(message = "Shop name is required")
    private String name;
    private String legalName;
    private String phone;
    private String email;
    private String address;
    private Long ownerAdminId;
    private SubscriptionPlan subscriptionPlan;
    private Boolean active;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Long getOwnerAdminId() { return ownerAdminId; }
    public void setOwnerAdminId(Long ownerAdminId) { this.ownerAdminId = ownerAdminId; }
    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
