package com.aem.tiretrack.dto.auth;

import java.util.List;

import com.aem.tiretrack.enums.SubscriptionPlan;
import com.aem.tiretrack.enums.UserRole;

public class LoginResponse {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String message;
    private String token;
    private String refreshToken;
    private Long shopId;
    private String shopName;
    private SubscriptionPlan subscriptionPlan;
    private boolean multiLocationAllowed;
    private boolean shopOwner;
    private Long locationId;
    private String locationName;
    private List<Long> accessibleLocationIds;
    private List<String> permissions;

    public LoginResponse(Long id, String fullName, String email, UserRole role, String message, String token, String refreshToken) {
        this(id, fullName, email, role, message, token, refreshToken, null, null);
    }

    public LoginResponse(Long id, String fullName, String email, UserRole role, String message, String token, String refreshToken, Long shopId, String shopName) {
        this(id, fullName, email, role, message, token, refreshToken, shopId, shopName, null, false, false, null, null, List.of(), List.of());
    }

    public LoginResponse(
            Long id,
            String fullName,
            String email,
            UserRole role,
            String message,
            String token,
            String refreshToken,
            Long shopId,
            String shopName,
            SubscriptionPlan subscriptionPlan,
            boolean multiLocationAllowed,
            boolean shopOwner,
            Long locationId,
            String locationName,
            List<Long> accessibleLocationIds,
            List<String> permissions) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
        this.shopId = shopId;
        this.shopName = shopName;
        this.subscriptionPlan = subscriptionPlan;
        this.multiLocationAllowed = multiLocationAllowed;
        this.shopOwner = shopOwner;
        this.locationId = locationId;
        this.locationName = locationName;
        this.accessibleLocationIds = accessibleLocationIds == null ? List.of() : accessibleLocationIds;
        this.permissions = permissions == null ? List.of() : permissions;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public boolean isMultiLocationAllowed() {
        return multiLocationAllowed;
    }

    public boolean isShopOwner() {
        return shopOwner;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public List<Long> getAccessibleLocationIds() {
        return accessibleLocationIds;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
