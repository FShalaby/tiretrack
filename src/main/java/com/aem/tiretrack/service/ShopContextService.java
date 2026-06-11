package com.aem.tiretrack.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class ShopContextService {
    private final UserRepository userRepository;
    private final ShopLocationRepository shopLocationRepository;

    public ShopContextService(UserRepository userRepository, ShopLocationRepository shopLocationRepository) {
        this.userRepository = userRepository;
        this.shopLocationRepository = shopLocationRepository;
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return Optional.empty();
        }

        return userRepository.findByEmail(authentication.getName());
    }

    public Optional<Shop> getCurrentShop() {
        return getCurrentUser().map(User::getShop);
    }

    public Optional<Shop> getCurrentUserShop() {
        return getCurrentShop();
    }

    public Optional<Long> getCurrentUserShopId() {
        return getCurrentUserShop().map(Shop::getId);
    }

    public Optional<ShopLocation> getCurrentLocation() {
        return getCurrentUser().map(User::getShopLocation);
    }

    public Optional<Shop> getCurrentTenantShop() {
        return getCurrentUser()
                .filter(this::isTenantOperator)
                .map(User::getShop);
    }

    public Optional<ShopLocation> getCurrentTenantLocation() {
        return getCurrentUser()
                .filter(this::isTenantOperator)
                .map(User::getShopLocation);
    }

    public Optional<Long> getCurrentTenantShopId() {
        return getCurrentTenantShop().map(Shop::getId);
    }

    public Optional<Long> getCurrentTenantLocationId() {
        return getCurrentTenantLocation().map(ShopLocation::getId);
    }

    public boolean hasAssignedTenantShop() {
        return getCurrentTenantShopId().isPresent();
    }

    public boolean isSuperAdmin() {
        return getCurrentUser()
                .map(user -> user.getRole() == UserRole.SUPER_ADMIN)
                .orElse(false);
    }

    public boolean isShopAdmin() {
        return getCurrentUser()
                .map(user -> user.getRole() == UserRole.OWNER || user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    public boolean isLocationScoped() {
        return getCurrentTenantLocationId().isPresent();
    }

    public Shop requireCurrentShop() {
        return getCurrentShop()
                .orElseThrow(() -> new AccessDeniedException("No shop assigned to this user. Contact platform administrator."));
    }

    public Shop requireShopForAdminOrEmployee() {
        User user = getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication is required."));

        if (!isTenantOperator(user)) {
            return user.getShop();
        }

        if (user.getShop() == null) {
            throw new AccessDeniedException("No shop assigned to this user. Contact platform administrator.");
        }

        return user.getShop();
    }

    public List<Long> getAccessibleLocationIds(User user) {
        if (user == null || user.getRole() == UserRole.SUPER_ADMIN || user.getShop() == null) {
            return List.of();
        }

        if (!isShopWideManager(user) && user.getShopLocation() != null) {
            return List.of(user.getShopLocation().getId());
        }

        return shopLocationRepository.findByShop_IdAndActiveTrue(user.getShop().getId()).stream()
                .map(ShopLocation::getId)
                .toList();
    }

    public boolean canAccessShop(Long shopId) {
        if (shopId == null || isSuperAdmin()) {
            return true;
        }

        return getCurrentUserShop()
                .map(shop -> shop.getId().equals(shopId))
                .orElse(false);
    }

    public boolean canAccessTenantShop(Shop resourceShop) {
        if (isSuperAdmin()) {
            return true;
        }

        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return true;
        }

        User user = currentUser.get();
        if (!isTenantOperator(user)) {
            return resourceShop == null || (user.getShop() != null && user.getShop().getId().equals(resourceShop.getId()));
        }

        Shop currentShop = requireShopForAdminOrEmployee();
        return resourceShop == null || currentShop.getId().equals(resourceShop.getId());
    }

    public boolean canAccessTenantResource(Shop resourceShop, ShopLocation resourceLocation) {
        if (!canAccessTenantShop(resourceShop)) {
            return false;
        }

        Optional<Long> currentLocationId = getCurrentTenantLocationId();
        if (currentLocationId.isEmpty()) {
            return true;
        }

        // Location assignment is still optional. Shop isolation is enforced above;
        // null locations remain visible within the user's shop until location data is fully migrated.
        return resourceLocation == null || currentLocationId.get().equals(resourceLocation.getId());
    }

    public boolean isTenantOperator(User user) {
        return user != null
                && (user.getRole() == UserRole.OWNER
                    || user.getRole() == UserRole.ADMIN
                    || user.getRole() == UserRole.EMPLOYEE);
    }

    public boolean isShopWideManager(User user) {
        if (user == null) {
            return false;
        }

        if (user.getRole() == UserRole.OWNER) {
            return true;
        }

        // Backward compatibility: existing ADMIN users created before OWNER existed
        // may still have no location assignment and must continue as shop-wide managers
        // until the platform owner migrates them to OWNER or a specific location.
        return user.getRole() == UserRole.ADMIN && user.getShopLocation() == null;
    }

    public boolean canAccessTenantUser(User user) {
        if (user == null) {
            return false;
        }

        return canAccessTenantResource(user.getShop(), user.getShopLocation());
    }

    public void requireShopAccess(Long shopId) {
        if (!canAccessShop(shopId)) {
            throw new AccessDeniedException("You do not have permission to access this shop resource.");
        }
    }

    public Optional<ShopLocation> resolveAccessibleLocation(Long locationId, Shop expectedShop, boolean requireActive) {
        if (locationId == null) {
            return Optional.empty();
        }

        ShopLocation location = shopLocationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Shop location not found with id: " + locationId));

        if (requireActive && !location.isActive()) {
            throw new IllegalArgumentException("Selected shop location is inactive.");
        }

        Shop locationShop = location.getShop();
        if (locationShop == null) {
            throw new IllegalArgumentException("Selected shop location is not linked to a shop.");
        }

        if (expectedShop != null && !locationShop.getId().equals(expectedShop.getId())) {
            throw new AccessDeniedException("Selected location does not belong to this shop.");
        }

        Optional<Shop> currentTenantShop = getCurrentTenantShop();
        if (currentTenantShop.isPresent() && !locationShop.getId().equals(currentTenantShop.get().getId())) {
            throw new AccessDeniedException("You do not have permission to access this shop location.");
        }

        if (!canAccessTenantResource(locationShop, location)) {
            throw new AccessDeniedException("You do not have permission to access this shop location.");
        }

        return Optional.of(location);
    }

    public boolean canUseCustomerFacingLocation(ShopLocation location) {
        if (location == null || !location.isActive() || !location.isCustomerFacing()) {
            return false;
        }

        return switch (location.getType()) {
            case STORE, MOBILE, MOBILE_SERVICE -> true;
            case STORAGE, WAREHOUSE, OTHER -> false;
        };
    }
}
