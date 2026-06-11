package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.ShopLocationRequest;
import com.aem.tiretrack.enums.ShopLocationType;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class ShopLocationService {
    private final ShopLocationRepository locationRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final PlanAccessService planAccessService;

    public ShopLocationService(
            ShopLocationRepository locationRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            PlanAccessService planAccessService) {
        this.locationRepository = locationRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.planAccessService = planAccessService;
    }

    public List<ShopLocation> getLocationsByShop(Long shopId) {
        User currentUser = currentUser();
        if (isLocationScopedForShop(currentUser, shopId)) {
            return List.of(currentUser.getShopLocation());
        }

        ensureCanViewShop(shopId, currentUser);
        return locationRepository.findByShop_Id(shopId);
    }

    public List<ShopLocation> getActiveLocationsByShop(Long shopId) {
        User currentUser = currentUser();
        if (isLocationScopedForShop(currentUser, shopId)) {
            ShopLocation location = currentUser.getShopLocation();
            return location.isActive() ? List.of(location) : List.of();
        }

        ensureCanViewShop(shopId, currentUser);
        return locationRepository.findByShop_IdAndActiveTrue(shopId);
    }

    public ShopLocation getLocationById(Long id) {
        ShopLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shop location not found with id: " + id));
        ensureCanViewLocation(location);
        return location;
    }

    @Transactional
    public ShopLocation createLocation(ShopLocationRequest request) {
        Shop shop = getActiveShop(request.getShopId());
        ensureCanManageShop(shop.getId());

        ShopLocation location = new ShopLocation();
        location.setShop(shop);
        applyRequest(location, request, true);
        enforceLocationLimit(location, null);
        return locationRepository.save(location);
    }

    @Transactional
    public ShopLocation updateLocation(Long id, ShopLocationRequest request) {
        ShopLocation location = getLocationById(id);
        ensureCanManageShop(location.getShop().getId());
        Shop previousShop = location.getShop();
        boolean wasActive = location.isActive();
        Long existingActiveLocationId = null;

        if (request.getShopId() != null && !request.getShopId().equals(previousShop.getId())) {
            Shop newShop = getActiveShop(request.getShopId());
            ensureCanManageShop(newShop.getId());
            location.setShop(newShop);
        }

        applyRequest(location, request, false);

        if (wasActive && previousShop.getId().equals(location.getShop().getId())) {
            existingActiveLocationId = location.getId();
        }

        enforceLocationLimit(location, existingActiveLocationId);
        return locationRepository.save(location);
    }

    @Transactional
    public ShopLocation activateLocation(Long id) {
        ShopLocation location = getLocationById(id);
        ensureCanManageShop(location.getShop().getId());
        location.setActive(true);
        enforceLocationLimit(location, location.getId());
        return locationRepository.save(location);
    }

    @Transactional
    public ShopLocation deactivateLocation(Long id) {
        ShopLocation location = getLocationById(id);
        ensureCanManageShop(location.getShop().getId());
        location.setActive(false);
        return locationRepository.save(location);
    }

    private void applyRequest(ShopLocation location, ShopLocationRequest request, boolean creating) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Location name is required");
        }

        location.setName(request.getName().trim());
        location.setType(request.getType() == null ? ShopLocationType.STORE : request.getType());
        location.setAddress(request.getAddress());
        location.setCity(request.getCity());
        location.setProvince(request.getProvince());
        location.setPostalCode(request.getPostalCode());
        location.setPhone(request.getPhone());
        location.setEmail(request.getEmail());

        if (request.getActive() != null) {
            location.setActive(request.getActive());
        } else if (creating) {
            location.setActive(true);
        }

        if (request.getCustomerFacing() != null) {
            location.setCustomerFacing(request.getCustomerFacing());
        } else if (creating) {
            location.setCustomerFacing(isDefaultCustomerFacingType(location.getType()));
        }
    }

    private boolean isDefaultCustomerFacingType(ShopLocationType type) {
        return type == ShopLocationType.STORE
                || type == ShopLocationType.MOBILE
                || type == ShopLocationType.MOBILE_SERVICE;
    }

    private Shop getActiveShop(Long shopId) {
        if (shopId == null) {
            User currentUser = currentUser();
            if (isShopOwnerForShop(currentUser, currentUser.getShop())) {
                Shop shop = currentUser.getShop();
                if (!shop.isActive()) {
                    throw new IllegalArgumentException("Cannot manage locations for an inactive shop");
                }
                return shop;
            }
            throw new IllegalArgumentException("Shop is required");
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));

        if (!shop.isActive()) {
            throw new IllegalArgumentException("Cannot manage locations for an inactive shop");
        }

        return shop;
    }

    private void enforceLocationLimit(ShopLocation location, Long existingActiveLocationId) {
        if (!location.isActive()) {
            return;
        }

        Shop shop = location.getShop();
        long activeCount = locationRepository.countByShop_IdAndActiveTrue(shop.getId());

        if (existingActiveLocationId != null) {
            activeCount -= 1;
        }

        if (activeCount >= planAccessService.getMaxLocations(shop)) {
            throw new IllegalArgumentException("Multi-location support requires a Premium plan.");
        }
    }

    private void ensureCanManageShop(Long shopId) {
        if (shopId == null) {
            throw new IllegalArgumentException("Shop is required");
        }

        User currentUser = currentUser();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }

        if (isShopOwnerForShop(currentUser, shop)) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to manage this shop location");
    }

    private void ensureCanViewShop(Long shopId, User currentUser) {
        if (shopId == null) {
            throw new IllegalArgumentException("Shop is required");
        }

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));

        if (isShopOwnerForShop(currentUser, shop)) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to view this shop location");
    }

    private void ensureCanViewLocation(ShopLocation location) {
        User currentUser = currentUser();

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }

        if (isShopOwnerForShop(currentUser, location.getShop())) {
            return;
        }

        if (currentUser.getRole() == UserRole.ADMIN
                && currentUser.getShopLocation() != null
                && currentUser.getShopLocation().getId().equals(location.getId())) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to view this shop location");
    }

    private boolean isShopOwnerForShop(User user, Shop shop) {
        if (user == null || shop == null || user.getShop() == null || !user.getShop().getId().equals(shop.getId())) {
            return false;
        }

        if (user.getRole() == UserRole.OWNER) {
            return true;
        }

        return user.getRole() == UserRole.ADMIN
                && shop.getOwnerAdminId() != null
                && shop.getOwnerAdminId().equals(user.getId());
    }

    private boolean isLocationScopedForShop(User currentUser, Long shopId) {
        return currentUser.getRole() == UserRole.ADMIN
                && currentUser.getShop() != null
                && currentUser.getShopLocation() != null
                && currentUser.getShop().getId().equals(shopId);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Authentication is required to manage shop locations");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
