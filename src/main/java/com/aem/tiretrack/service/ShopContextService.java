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
                .filter(user -> user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EMPLOYEE)
                .map(User::getShop);
    }

    public Optional<ShopLocation> getCurrentTenantLocation() {
        return getCurrentUser()
                .filter(user -> user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EMPLOYEE)
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
                .map(user -> user.getRole() == UserRole.ADMIN)
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

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.EMPLOYEE) {
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

        if (user.getShopLocation() != null) {
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
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.EMPLOYEE) {
            return resourceShop == null || (user.getShop() != null && user.getShop().getId().equals(resourceShop.getId()));
        }

        Shop currentShop = requireShopForAdminOrEmployee();
        return resourceShop != null && currentShop.getId().equals(resourceShop.getId());
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
}
