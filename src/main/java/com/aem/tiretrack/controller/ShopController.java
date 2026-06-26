package com.aem.tiretrack.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.ShopRequest;
import com.aem.tiretrack.dto.ShopResponse;
import com.aem.tiretrack.dto.UserResponse;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.service.PlatformLinkService;
import com.aem.tiretrack.service.ShopService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/platform")
public class ShopController {
    private final ShopService shopService;
    private final PlatformLinkService platformLinkService;

    public ShopController(ShopService shopService, PlatformLinkService platformLinkService) {
        this.shopService = shopService;
        this.platformLinkService = platformLinkService;
    }

    @GetMapping("/shops")
    public List<ShopResponse> getAllShops() {
        return shopService.getAllShops().stream().map(this::toResponse).toList();
    }

    @GetMapping("/shops/{id}")
    public ShopResponse getShopById(@PathVariable Long id) {
        return toResponse(shopService.getShopById(id));
    }

    @PostMapping("/shops")
    public ShopResponse createShop(@Valid @RequestBody ShopRequest request) {
        return toResponse(shopService.createShop(request));
    }

    @PutMapping("/shops/{id}")
    public ShopResponse updateShop(@PathVariable Long id, @Valid @RequestBody ShopRequest request) {
        return toResponse(shopService.updateShop(id, request));
    }

    @PostMapping("/shops/{id}/activate")
    public ShopResponse activateShop(@PathVariable Long id) {
        return toResponse(shopService.activateShop(id));
    }

    @PostMapping("/shops/{id}/deactivate")
    public ShopResponse deactivateShop(@PathVariable Long id) {
        return toResponse(shopService.deactivateShop(id));
    }

    @DeleteMapping("/shops/{id}")
    public Map<String, String> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return Map.of("message", "Shop and linked data deleted.");
    }

    @PostMapping("/shops/{shopId}/assign-legacy-data")
    public Map<String, String> assignLegacyData(@PathVariable Long shopId) {
        Shop shop = shopService.getShopById(shopId);
        platformLinkService.transferLegacySingleShopData(shop);
        return Map.of("message", "Legacy unassigned data linked to " + shop.getName() + ".");
    }

    @PutMapping("/users/{userId}/shop/{shopId}")
    public UserResponse assignUserToShop(@PathVariable Long userId, @PathVariable Long shopId) {
        return new UserResponse(shopService.assignUserToShop(userId, shopId));
    }

    @PutMapping("/users/{userId}/assign-shop/{shopId}")
    public UserResponse assignAdminToShop(@PathVariable Long userId, @PathVariable Long shopId) {
        return new UserResponse(shopService.assignAdminToShop(userId, shopId));
    }

    @PutMapping("/users/{userId}/assign-location/{locationId}")
    public UserResponse assignAdminToLocation(@PathVariable Long userId, @PathVariable Long locationId) {
        return new UserResponse(shopService.assignAdminToLocation(userId, locationId));
    }

    private ShopResponse toResponse(Shop shop) {
        return new ShopResponse(shop, shopService.getActiveLocationCount(shop), shopService.getMaxLocations(shop));
    }
}
