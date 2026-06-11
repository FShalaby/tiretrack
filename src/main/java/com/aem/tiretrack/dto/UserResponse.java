package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.EmploymentType;
import com.aem.tiretrack.enums.SubscriptionPlan;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.User;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private BigDecimal hourlyRate;
    private boolean payrollEnabled;
    private EmploymentType employmentType;
    private Long shopId;
    private String shopName;
    private SubscriptionPlan subscriptionPlan;
    private boolean multiLocationAllowed;
    private boolean shopOwner;
    private Long locationId;
    private String locationName;

    public UserResponse(User user) {
        Shop shop = user.getShop();

        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
        this.hourlyRate = user.getHourlyRate();
        this.payrollEnabled = user.isPayrollEnabled();
        this.employmentType = user.getEmploymentType();
        this.shopId = shop == null ? null : shop.getId();
        this.shopName = shop == null ? null : shop.getName();
        this.subscriptionPlan = shop == null ? null : shop.getSubscriptionPlan();
        this.multiLocationAllowed = shop != null && shop.hasMultiLocationAccess();
        this.shopOwner = isShopOwner(user, shop);
        this.locationId = user.getShopLocation() == null ? null : user.getShopLocation().getId();
        this.locationName = user.getShopLocation() == null ? null : user.getShopLocation().getName();
    }

    private boolean isShopOwner(User user, Shop shop) {
        if (user.getRole() == UserRole.OWNER) {
            return true;
        }

        return shop != null
                && shop.getOwnerAdminId() != null
                && shop.getOwnerAdminId().equals(user.getId());
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public boolean isPayrollEnabled() { return payrollEnabled; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public boolean isMultiLocationAllowed() { return multiLocationAllowed; }
    public boolean isShopOwner() { return shopOwner; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
}
