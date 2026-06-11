package com.aem.tiretrack.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.booking.PublicBookingRequest;
import com.aem.tiretrack.dto.booking.PublicBookingResponse;
import com.aem.tiretrack.dto.booking.PublicShopLocationResponse;
import com.aem.tiretrack.dto.booking.PublicShopResponse;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.service.AppointmentService;
import com.aem.tiretrack.service.ShopContextService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicBookingController {

    private final AppointmentService appointmentService;
    private final ShopRepository shopRepository;
    private final ShopLocationRepository shopLocationRepository;
    private final ShopContextService shopContextService;

    public PublicBookingController(
            AppointmentService appointmentService,
            ShopRepository shopRepository,
            ShopLocationRepository shopLocationRepository,
            ShopContextService shopContextService) {
        this.appointmentService = appointmentService;
        this.shopRepository = shopRepository;
        this.shopLocationRepository = shopLocationRepository;
        this.shopContextService = shopContextService;
    }

    @GetMapping("/shops")
    public List<PublicShopResponse> getPublicShops() {
        return shopRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(PublicShopResponse::new)
                .toList();
    }

    @GetMapping("/shops/{shopId}/locations")
    public List<PublicShopLocationResponse> getPublicShopLocations(@org.springframework.web.bind.annotation.PathVariable Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .filter(Shop::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        return shopLocationRepository.findByShop_IdAndActiveTrueAndCustomerFacingTrue(shop.getId()).stream()
                .filter(shopContextService::canUseCustomerFacingLocation)
                .map(PublicShopLocationResponse::new)
                .toList();
    }

    @PostMapping("/bookings")
    public PublicBookingResponse createPublicBooking(@Valid @RequestBody PublicBookingRequest request) {
        Appointment appointment = new Appointment();
        appointment.setCustomerName(request.getCustomerName());
        appointment.setEmail(request.getEmail());
        appointment.setPhone(request.getPhone());
        appointment.setVehicle(request.getVehicle());
        appointment.setTireSize(request.getTireSize());
        appointment.setAppointmentDate(request.getAppointmentDate().atTime(request.getAppointmentTime()));
        appointment.setServiceType(request.getServiceType());
        appointment.setNotes(request.getNotes());
        applyPublicTenantSelection(appointment, request);

        return new PublicBookingResponse(appointmentService.saveAppointment(appointment));
    }

    @GetMapping("/available-slots")
    public List<String> getAvailableSlots(
            @RequestParam LocalDate date,
            @RequestParam(required = false) Long locationId) {
        if (locationId != null) {
            ShopLocation location = shopLocationRepository.findById(locationId)
                    .orElseThrow(() -> new IllegalArgumentException("Shop location not found"));
            if (!shopContextService.canUseCustomerFacingLocation(location)) {
                throw new IllegalArgumentException("This location is not available for online booking.");
            }
        }

        return appointmentService.getAvailableSlots(date, locationId);
    }

    private void applyPublicTenantSelection(Appointment appointment, PublicBookingRequest request) {
        Shop shop = null;
        if (request.getShopId() != null) {
            shop = shopRepository.findById(request.getShopId())
                    .filter(Shop::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
            appointment.setShop(shop);
        }

        if (request.getLocationId() != null) {
            ShopLocation location = shopLocationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Shop location not found"));
            if (!shopContextService.canUseCustomerFacingLocation(location)) {
                throw new IllegalArgumentException("This location is not available for online booking.");
            }
            if (shop != null && (location.getShop() == null || !location.getShop().getId().equals(shop.getId()))) {
                throw new org.springframework.security.access.AccessDeniedException("Selected location does not belong to this shop.");
            }
            appointment.setShopLocation(location);
            if (appointment.getShop() == null) {
                appointment.setShop(location.getShop());
            }
        }
    }
}
