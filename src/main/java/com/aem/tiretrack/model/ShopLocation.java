package com.aem.tiretrack.model;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.ShopLocationType;
import com.aem.tiretrack.util.PhoneNumberUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "shop_locations")
public class ShopLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @NotBlank(message = "Location name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Location type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopLocationType type = ShopLocationType.STORE;

    private String address;

    private String city;

    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    private String phone;

    private String email;

    @Column(name = "customer_facing")
    private Boolean customerFacing = true;

    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (type == null) {
            type = ShopLocationType.STORE;
        }
        if (customerFacing == null) {
            customerFacing = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (type == null) {
            type = ShopLocationType.STORE;
        }
        if (customerFacing == null) {
            customerFacing = true;
        }
    }

    public Long getId() { return id; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
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
    public void setPhone(String phone) { this.phone = PhoneNumberUtils.formatCanadian(phone); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isCustomerFacing() { return customerFacing == null || customerFacing; }
    public void setCustomerFacing(Boolean customerFacing) { this.customerFacing = customerFacing == null ? true : customerFacing; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
