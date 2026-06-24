package com.aem.tiretrack.model;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.SubscriptionPlan;
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
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Shop name is required")
    @Column(nullable = false)
    private String name;

    @Column(name = "legal_name")
    private String legalName;

    private String phone;
    private String email;
    private String address;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_admin_id")
    private User ownerAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

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

        if (subscriptionPlan == null) {
            subscriptionPlan = SubscriptionPlan.BASIC;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (subscriptionPlan == null) {
            subscriptionPlan = SubscriptionPlan.BASIC;
        }
    }

    public boolean hasPremiumAccess() {
        return subscriptionPlan == SubscriptionPlan.PREMIUM || subscriptionPlan == SubscriptionPlan.ENTERPRISE;
    }

    public boolean hasMultiLocationAccess() {
        return hasPremiumAccess();
    }

    public boolean hasEnterpriseAccess() {
        return subscriptionPlan == SubscriptionPlan.ENTERPRISE;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = PhoneNumberUtils.formatCanadian(phone); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public User getOwnerAdmin() { return ownerAdmin; }
    public void setOwnerAdmin(User ownerAdmin) { this.ownerAdmin = ownerAdmin; }
    public Long getOwnerAdminId() { return ownerAdmin == null ? null : ownerAdmin.getId(); }
    public String getOwnerAdminName() { return ownerAdmin == null ? null : ownerAdmin.getFullName(); }
    public String getOwnerAdminEmail() { return ownerAdmin == null ? null : ownerAdmin.getEmail(); }
    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
