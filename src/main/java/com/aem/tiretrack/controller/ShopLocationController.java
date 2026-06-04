package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.ShopLocationRequest;
import com.aem.tiretrack.dto.ShopLocationResponse;
import com.aem.tiretrack.service.ShopLocationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shop-locations")
public class ShopLocationController {
    private final ShopLocationService shopLocationService;

    public ShopLocationController(ShopLocationService shopLocationService) {
        this.shopLocationService = shopLocationService;
    }

    @GetMapping("/shop/{shopId}")
    public List<ShopLocationResponse> getLocationsByShop(@PathVariable Long shopId) {
        return shopLocationService.getLocationsByShop(shopId).stream().map(ShopLocationResponse::new).toList();
    }

    @GetMapping("/shop/{shopId}/active")
    public List<ShopLocationResponse> getActiveLocationsByShop(@PathVariable Long shopId) {
        return shopLocationService.getActiveLocationsByShop(shopId).stream().map(ShopLocationResponse::new).toList();
    }

    @GetMapping("/{id}")
    public ShopLocationResponse getLocationById(@PathVariable Long id) {
        return new ShopLocationResponse(shopLocationService.getLocationById(id));
    }

    @PostMapping
    public ShopLocationResponse createLocation(@Valid @RequestBody ShopLocationRequest request) {
        return new ShopLocationResponse(shopLocationService.createLocation(request));
    }

    @PutMapping("/{id}")
    public ShopLocationResponse updateLocation(@PathVariable Long id, @Valid @RequestBody ShopLocationRequest request) {
        return new ShopLocationResponse(shopLocationService.updateLocation(id, request));
    }

    @PostMapping("/{id}/activate")
    public ShopLocationResponse activateLocation(@PathVariable Long id) {
        return new ShopLocationResponse(shopLocationService.activateLocation(id));
    }

    @PostMapping("/{id}/deactivate")
    public ShopLocationResponse deactivateLocation(@PathVariable Long id) {
        return new ShopLocationResponse(shopLocationService.deactivateLocation(id));
    }
}
