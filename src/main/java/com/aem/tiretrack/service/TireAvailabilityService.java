package com.aem.tiretrack.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.aem.tiretrack.dto.TireAvailabilityResponse;
import com.aem.tiretrack.dto.TireLocationAvailabilityResponse;
import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.enums.ShopLocationType;
import com.aem.tiretrack.enums.TireAvailabilityStatus;
import com.aem.tiretrack.exception.ResourceNotFoundException;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.repository.CustomerVehicleRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.util.TireSizeUtils;
import com.aem.tiretrack.util.TireSizeUtils.TireSizeSpec;

@Service
public class TireAvailabilityService {
    private static final int LOW_STOCK_THRESHOLD = 4;

    private final TireRepository tireRepository;
    private final CustomerVehicleRepository vehicleRepository;
    private final ShopContextService shopContextService;

    public TireAvailabilityService(
            TireRepository tireRepository,
            CustomerVehicleRepository vehicleRepository,
            ShopContextService shopContextService) {
        this.tireRepository = tireRepository;
        this.vehicleRepository = vehicleRepository;
        this.shopContextService = shopContextService;
    }

    public TireAvailabilityResponse checkStaffAvailability(Long vehicleId, Long locationId, ServiceType serviceType) {
        CustomerVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!shopContextService.canAccessTenantResource(vehicle.getShop(), vehicle.getShopLocation())) {
            throw new AccessDeniedException("You do not have permission to access this vehicle.");
        }

        Shop shop = resolveVehicleShop(vehicle);
        ShopLocation location = resolveLocation(locationId, shop, false);
        return checkAvailability(vehicle, shop, location, serviceType, false);
    }

    public TireAvailabilityResponse checkCustomerAvailability(CustomerVehicle vehicle, ShopLocation location, ServiceType serviceType) {
        Shop shop = resolveVehicleShop(vehicle);
        return checkAvailability(vehicle, shop, location, serviceType, true);
    }

    public boolean isTireDependentService(ServiceType serviceType) {
        return serviceType == ServiceType.INSTALLATION;
    }

    public boolean vehicleHasTireSize(CustomerVehicle vehicle) {
        return !requiredTires(vehicle).isEmpty();
    }

    public String requiredSizeLabel(CustomerVehicle vehicle) {
        List<RequiredTire> requiredTires = requiredTires(vehicle);
        if (requiredTires.isEmpty()) {
            return "";
        }

        if (requiredTires.size() == 1) {
            return requiredTires.get(0).spec().display();
        }

        return "Front: " + requiredTires.get(0).spec().display()
                + " / Rear: " + requiredTires.get(1).spec().display();
    }

    private TireAvailabilityResponse checkAvailability(
            CustomerVehicle vehicle,
            Shop shop,
            ShopLocation selectedLocation,
            ServiceType serviceType,
            boolean customerView) {
        boolean tireServiceRequired = isTireDependentService(serviceType);
        List<RequiredTire> requiredTires = requiredTires(vehicle);
        String requiredSize = requiredSizeLabel(vehicle);

        if (!tireServiceRequired) {
            return new TireAvailabilityResponse(
                    vehicle.getId(),
                    requiredSize,
                    TireAvailabilityStatus.IN_STOCK,
                    0,
                    List.of(),
                    List.of(),
                    false,
                    true,
                    "This service does not require tire inventory.");
        }

        if (requiredTires.isEmpty()) {
            return new TireAvailabilityResponse(
                    vehicle.getId(),
                    "",
                    TireAvailabilityStatus.OUT_OF_STOCK,
                    0,
                    List.of(),
                    List.of(),
                    true,
                    false,
                    "No saved tire size is available for this vehicle.");
        }

        AvailabilityAggregate aggregate = new AvailabilityAggregate();
        List<PerSizeAvailability> perSizeResults = requiredTires.stream()
                .map(required -> checkRequiredTire(required, shop, selectedLocation))
                .toList();

        for (PerSizeAvailability result : perSizeResults) {
            aggregate.selectedQuantity += result.selectedQuantity();
            aggregate.status = worseStatus(aggregate.status, result.status());
            result.otherLocations().forEach((location, quantity) -> aggregate.addOther(location, quantity));
            result.warehouseLocations().forEach((location, quantity) -> aggregate.addWarehouse(location, quantity));
        }

        List<TireLocationAvailabilityResponse> otherLocations = aggregate.otherAvailability(customerView);
        List<TireLocationAvailabilityResponse> warehouseLocations = customerView ? List.of() : aggregate.warehouseAvailability();
        TireAvailabilityStatus status = aggregate.status == null ? TireAvailabilityStatus.OUT_OF_STOCK : aggregate.status;
        boolean canConfirm = status == TireAvailabilityStatus.IN_STOCK || status == TireAvailabilityStatus.LOW_STOCK;

        return new TireAvailabilityResponse(
                vehicle.getId(),
                requiredSize,
                status,
                aggregate.selectedQuantity,
                otherLocations,
                warehouseLocations,
                true,
                canConfirm,
                reason(status, requiredSize, aggregate.selectedQuantity));
    }

    private PerSizeAvailability checkRequiredTire(RequiredTire required, Shop shop, ShopLocation selectedLocation) {
        List<Tire> matches = matchingTires(required.spec(), shop).stream()
                .filter(tire -> tire.getAvailableQuantity() > 0)
                .filter(tire -> shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation()))
                .toList();

        int selectedQuantity = matches.stream()
                .filter(tire -> sameLocation(tire.getShopLocation(), selectedLocation))
                .mapToInt(Tire::getAvailableQuantity)
                .sum();

        Map<ShopLocation, Integer> otherLocations = new HashMap<>();
        Map<ShopLocation, Integer> warehouseLocations = new HashMap<>();

        for (Tire tire : matches) {
            ShopLocation location = tire.getShopLocation();
            if (sameLocation(location, selectedLocation)) {
                continue;
            }

            if (isInternalStockLocation(location)) {
                warehouseLocations.merge(location, tire.getAvailableQuantity(), Integer::sum);
            } else {
                otherLocations.merge(location, tire.getAvailableQuantity(), Integer::sum);
            }
        }

        int otherQuantity = otherLocations.values().stream().mapToInt(Integer::intValue).sum()
                + warehouseLocations.values().stream().mapToInt(Integer::intValue).sum();
        TireAvailabilityStatus status;
        if (selectedQuantity >= required.quantity()) {
            status = selectedQuantity <= LOW_STOCK_THRESHOLD ? TireAvailabilityStatus.LOW_STOCK : TireAvailabilityStatus.IN_STOCK;
        } else if (selectedQuantity > 0) {
            status = TireAvailabilityStatus.LOW_STOCK;
        } else if (otherQuantity > 0) {
            status = TireAvailabilityStatus.AVAILABLE_AT_OTHER_LOCATION;
        } else {
            status = TireAvailabilityStatus.OUT_OF_STOCK;
        }

        return new PerSizeAvailability(status, selectedQuantity, otherLocations, warehouseLocations);
    }

    private List<Tire> matchingTires(TireSizeSpec spec, Shop shop) {
        if (shop != null) {
            return tireRepository.findByShop_IdAndWidthAndAspectRatioAndRimSize(
                    shop.getId(),
                    spec.width(),
                    spec.aspectRatio(),
                    spec.rimSize());
        }

        return tireRepository.findByWidthAndAspectRatioAndRimSize(
                spec.width(),
                spec.aspectRatio(),
                spec.rimSize());
    }

    private List<RequiredTire> requiredTires(CustomerVehicle vehicle) {
        if (vehicle == null) {
            return List.of();
        }

        if ("staggered".equalsIgnoreCase(vehicle.getTireSetup())) {
            List<RequiredTire> tires = new ArrayList<>();
            TireSizeUtils.parsePassengerSize(vehicle.getFrontTireSize()).ifPresent(size -> tires.add(new RequiredTire(size, 2)));
            TireSizeUtils.parsePassengerSize(vehicle.getRearTireSize()).ifPresent(size -> tires.add(new RequiredTire(size, 2)));
            return tires;
        }

        Optional<TireSizeSpec> size = TireSizeUtils.parsePassengerSize(vehicle.getTireSize());
        return size.map(spec -> List.of(new RequiredTire(spec, 4))).orElseGet(List::of);
    }

    private Shop resolveVehicleShop(CustomerVehicle vehicle) {
        if (vehicle.getShop() != null) {
            return vehicle.getShop();
        }

        return vehicle.getCustomer() == null ? null : vehicle.getCustomer().getShop();
    }

    private ShopLocation resolveLocation(Long locationId, Shop expectedShop, boolean requireCustomerFacing) {
        ShopLocation location = shopContextService.resolveAccessibleLocation(locationId, expectedShop, true).orElse(null);
        if (requireCustomerFacing && location != null && !shopContextService.canUseCustomerFacingLocation(location)) {
            throw new IllegalArgumentException("This location is not available for online booking.");
        }
        return location;
    }

    private boolean sameLocation(ShopLocation first, ShopLocation second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }

        return first.getId() != null && first.getId().equals(second.getId());
    }

    private boolean isInternalStockLocation(ShopLocation location) {
        return location != null
                && (location.getType() == ShopLocationType.WAREHOUSE
                    || location.getType() == ShopLocationType.STORAGE);
    }

    private TireAvailabilityStatus worseStatus(TireAvailabilityStatus current, TireAvailabilityStatus next) {
        if (current == null) {
            return next;
        }

        return severity(next) > severity(current) ? next : current;
    }

    private int severity(TireAvailabilityStatus status) {
        return switch (status) {
            case IN_STOCK -> 0;
            case LOW_STOCK -> 1;
            case AVAILABLE_AT_OTHER_LOCATION -> 2;
            case OUT_OF_STOCK -> 3;
        };
    }

    private String reason(TireAvailabilityStatus status, String requiredSize, int selectedQuantity) {
        return switch (status) {
            case IN_STOCK -> "Tire size " + requiredSize + " is available at the selected location.";
            case LOW_STOCK -> "Low stock for tire size " + requiredSize + ". Available quantity: " + selectedQuantity + ".";
            case AVAILABLE_AT_OTHER_LOCATION -> "Tire size " + requiredSize + " is not stocked at the selected location, but exists elsewhere in this shop.";
            case OUT_OF_STOCK -> "Tire size " + requiredSize + " is not currently available.";
        };
    }

    private record RequiredTire(TireSizeSpec spec, int quantity) {}
    private record PerSizeAvailability(
            TireAvailabilityStatus status,
            int selectedQuantity,
            Map<ShopLocation, Integer> otherLocations,
            Map<ShopLocation, Integer> warehouseLocations) {}

    private class AvailabilityAggregate {
        private TireAvailabilityStatus status;
        private int selectedQuantity;
        private final Map<ShopLocation, Integer> otherLocations = new HashMap<>();
        private final Map<ShopLocation, Integer> warehouseLocations = new HashMap<>();

        void addOther(ShopLocation location, int quantity) {
            otherLocations.merge(location, quantity, Integer::sum);
        }

        void addWarehouse(ShopLocation location, int quantity) {
            warehouseLocations.merge(location, quantity, Integer::sum);
        }

        List<TireLocationAvailabilityResponse> otherAvailability(boolean customerView) {
            return otherLocations.entrySet().stream()
                    .filter(entry -> !customerView || shopContextService.canUseCustomerFacingLocation(entry.getKey()))
                    .filter(entry -> entry.getValue() > 0)
                    .sorted(locationEntryComparator())
                    .map(entry -> new TireLocationAvailabilityResponse(entry.getKey(), entry.getValue()))
                    .toList();
        }

        List<TireLocationAvailabilityResponse> warehouseAvailability() {
            return warehouseLocations.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .sorted(locationEntryComparator())
                    .map(entry -> new TireLocationAvailabilityResponse(entry.getKey(), entry.getValue()))
                    .toList();
        }

        private Comparator<Map.Entry<ShopLocation, Integer>> locationEntryComparator() {
            return Comparator
                    .comparing((Map.Entry<ShopLocation, Integer> entry) -> entry.getKey() == null ? "Unassigned" : entry.getKey().getName(), Comparator.nullsLast(String::compareToIgnoreCase))
                    .thenComparing(entry -> Objects.toString(entry.getKey() == null ? null : entry.getKey().getId(), ""));
        }
    }
}
