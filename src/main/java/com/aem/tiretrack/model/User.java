package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.EmploymentType;
import com.aem.tiretrack.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "users")
public class User 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Full name is required")
    @Column(name = "full_name")
    private String fullName;

    @NotNull(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotNull(message = "Phone is required")
    @Column(unique = true)
    private String phone;

    @NotNull(message = "Password is required")
    @Column(name = "password_hash")
    @JsonIgnore
    private String passwordHash;

    @NotNull(message = "Role is required")
    @Convert(converter = UserRoleConverter.class)
    private UserRole role;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "payroll_enabled")
    private Boolean payrollEnabled = false;

    @Convert(converter = EmploymentTypeConverter.class)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ShopLocation shopLocation;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


    public Long getId() {
    return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isPayrollEnabled() {
        return Boolean.TRUE.equals(payrollEnabled);
    }

    public void setPayrollEnabled(boolean payrollEnabled) {
        this.payrollEnabled = payrollEnabled;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public ShopLocation getShopLocation() {
        return shopLocation;
    }

    public void setShopLocation(ShopLocation shopLocation) {
        this.shopLocation = shopLocation;
    }

    public Long getLocationId() {
        return shopLocation == null ? null : shopLocation.getId();
    }

    public String getLocationName() {
        return shopLocation == null ? null : shopLocation.getName();
    }
}
