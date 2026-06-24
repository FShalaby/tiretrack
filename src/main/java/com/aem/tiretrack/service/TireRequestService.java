package com.aem.tiretrack.service;

import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.TireRequestCreateRequest;
import com.aem.tiretrack.dto.TireRequestStatusUpdateRequest;
import com.aem.tiretrack.dto.TireAvailabilityResponse;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.enums.TireRequestSource;
import com.aem.tiretrack.enums.TireRequestStatus;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.exception.ResourceNotFoundException;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.TireRequest;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.AppNotificationRepository;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.CustomerNotificationRepository;
import com.aem.tiretrack.repository.CustomerVehicleRepository;
import com.aem.tiretrack.repository.TireRequestRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class TireRequestService {
    private final TireRequestRepository tireRequestRepository;
    private final UserRepository userRepository;
    private final CustomerVehicleRepository vehicleRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerNotificationRepository customerNotificationRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final AuditLogService auditLogService;
    private final ShopContextService shopContextService;
    private final TireAvailabilityService tireAvailabilityService;

    public TireRequestService(
            TireRequestRepository tireRequestRepository,
            UserRepository userRepository,
            CustomerVehicleRepository vehicleRepository,
            AppointmentRepository appointmentRepository,
            CustomerNotificationRepository customerNotificationRepository,
            AppNotificationRepository appNotificationRepository,
            AuditLogService auditLogService,
            ShopContextService shopContextService,
            TireAvailabilityService tireAvailabilityService) {
        this.tireRequestRepository = tireRequestRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.appointmentRepository = appointmentRepository;
        this.customerNotificationRepository = customerNotificationRepository;
        this.appNotificationRepository = appNotificationRepository;
        this.auditLogService = auditLogService;
        this.shopContextService = shopContextService;
        this.tireAvailabilityService = tireAvailabilityService;
    }

    public List<TireRequest> getVisibleRequests() {
        if (shopContextService.isSuperAdmin()) {
            return tireRequestRepository.findAll().stream()
                    .sorted((first, second) -> second.getCreatedAt().compareTo(first.getCreatedAt()))
                    .toList();
        }

        Shop shop = shopContextService.requireShopForAdminOrEmployee();
        return tireRequestRepository.findByShop_IdOrderByCreatedAtDesc(shop.getId()).stream()
                .filter(this::canAccessRequest)
                .toList();
    }

    public List<TireRequest> getRequestsForCustomer(User customer) {
        return tireRequestRepository.findByCustomer_IdOrderByCreatedAtDesc(customer.getId()).stream()
                .filter(request -> belongsToCustomerShop(customer, request))
                .toList();
    }

    public boolean hasRequestForAppointment(Long appointmentId) {
        return appointmentId != null && tireRequestRepository.existsByAppointment_Id(appointmentId);
    }

    @Transactional
    public int markMatchingRequestsAvailableForTire(Tire tire) {
        if (tire == null || tire.getAvailableQuantity() <= 0) {
            return 0;
        }

        List<TireRequestStatus> openStatuses = List.of(
                TireRequestStatus.PENDING,
                TireRequestStatus.SOURCING,
                TireRequestStatus.UNAVAILABLE);

        int updatedCount = 0;
        for (TireRequest request : tireRequestRepository.findByStatusInOrderByCreatedAtDesc(openStatuses)) {
            if (!sameShop(request.getShop(), tire.getShop()) || request.getVehicle() == null) {
                continue;
            }

            TireAvailabilityResponse availability = tireAvailabilityService.checkCustomerAvailability(
                    request.getVehicle(),
                    request.getLocation(),
                    ServiceType.INSTALLATION);

            if (!availability.isCanConfirmAppointment()) {
                continue;
            }

            TireRequestStatus previousStatus = request.getStatus();
            request.setStatus(TireRequestStatus.AVAILABLE);
            request.setAdminResponse("Inventory was refilled and this tire is now available.");
            TireRequest saved = tireRequestRepository.save(request);
            updatedCount += 1;

            auditLogService.record(
                    "AUTO_AVAILABLE",
                    "TireRequest",
                    saved.getId(),
                    "Tire request changed from " + previousStatus + " to AVAILABLE after inventory refill.");
            notifyCustomer(saved, "Tire available", customerStatusMessage(saved));
            notifyStaff(saved, "Tire request now available", buildStaffRequestMessage(saved), "TIRE_REQUEST");
        }

        return updatedCount;
    }

    @Transactional
    public TireRequest createFromStaff(TireRequestCreateRequest request) {
        User currentUser = currentUser();
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Choose a customer for this tire request.");
        }
        if (request.getVehicleId() == null) {
            throw new IllegalArgumentException("Choose a vehicle for this tire request.");
        }

        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        CustomerVehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        Appointment appointment = request.getAppointmentId() == null ? null : appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        Shop shop = resolveRequestShop(customer, vehicle, appointment);
        ShopLocation location = resolveLocation(request.getLocationId(), shop, appointment, vehicle);

        if (!shopContextService.canAccessTenantResource(shop, location)) {
            throw new AccessDeniedException("You do not have permission to create this tire request.");
        }

        TireRequestSource source = request.getSource() == null ? sourceFor(currentUser) : request.getSource();
        return createRequest(customer, vehicle, shop, location, appointment, request.getRequestedSize(), source, currentUser.getId(), request.getNotes());
    }

    @Transactional
    public TireRequest createRequest(
            User customer,
            CustomerVehicle vehicle,
            Shop shop,
            ShopLocation location,
            Appointment appointment,
            String requestedSize,
            TireRequestSource source,
            Long requestedBy,
            String notes) {
        TireRequest tireRequest = new TireRequest();
        tireRequest.setCustomer(customer);
        tireRequest.setVehicle(vehicle);
        tireRequest.setShop(shop);
        tireRequest.setLocation(location);
        tireRequest.setAppointment(appointment);
        tireRequest.setRequestedSize(requestedSize);
        tireRequest.setSource(source);
        tireRequest.setRequestedBy(requestedBy);
        tireRequest.setStatus(TireRequestStatus.PENDING);
        tireRequest.setNotes(notes);

        TireRequest saved = tireRequestRepository.save(tireRequest);
        auditLogService.record("CREATED", "TireRequest", saved.getId(), "Created tire sourcing request for " + saved.getRequestedSize());
        notifyStaff(saved, "Tire sourcing request", buildStaffRequestMessage(saved), "TIRE_REQUEST");
        notifyCustomer(saved, "Tire request received", "Your tire request for " + saved.getRequestedSize() + " is waiting for shop review.");
        return saved;
    }

    @Transactional
    public TireRequest updateStatus(Long id, TireRequestStatusUpdateRequest request) {
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Choose a tire request status.");
        }

        TireRequest tireRequest = getRequestForCurrentUser(id);
        TireRequestStatus previousStatus = tireRequest.getStatus();
        tireRequest.setStatus(request.getStatus());
        tireRequest.setAdminResponse(request.getAdminResponse());
        TireRequest saved = tireRequestRepository.save(tireRequest);

        auditLogService.record(
                "STATUS_CHANGED",
                "TireRequest",
                saved.getId(),
                "Tire request status changed from " + previousStatus + " to " + saved.getStatus());
        notifyCustomer(saved, customerStatusTitle(saved.getStatus()), customerStatusMessage(saved));
        notifyStaff(saved, "Tire request updated", buildStaffRequestMessage(saved), "TIRE_REQUEST");
        return saved;
    }

    @Transactional
    public TireRequest confirmRelatedAppointment(Long id) {
        TireRequest request = getRequestForCurrentUser(id);
        Appointment appointment = request.getAppointment();
        if (appointment == null) {
            throw new IllegalArgumentException("This tire request is not linked to an appointment.");
        }
        if (request.getStatus() != TireRequestStatus.AVAILABLE && request.getStatus() != TireRequestStatus.SOURCING) {
            throw new IllegalArgumentException("Mark the tire request as sourcing or available before confirming the appointment.");
        }
        if (hasBookedConflict(appointment)) {
            throw new IllegalArgumentException("That appointment time is already booked. Choose a new appointment time first.");
        }

        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setConfirmationStatus("CONFIRMED");
        appointmentRepository.save(appointment);
        request.setStatus(TireRequestStatus.FULFILLED);
        TireRequest saved = tireRequestRepository.save(request);
        auditLogService.record("CONFIRMED", "Appointment", appointment.getId(), "Confirmed appointment after tire request #" + saved.getId());
        notifyCustomer(saved, "Appointment confirmed", "Your tire is available and the shop confirmed your appointment.");
        return saved;
    }

    public TireRequest getRequestForCurrentUser(Long id) {
        TireRequest request = tireRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tire request not found"));
        if (!canAccessRequest(request)) {
            throw new ResourceNotFoundException("Tire request not found");
        }
        return request;
    }

    private boolean hasBookedConflict(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) {
            return false;
        }

        return appointmentRepository.findByAppointmentDate(appointment.getAppointmentDate()).stream()
                .filter(existing -> !Objects.equals(existing.getId(), appointment.getId()))
                .filter(existing -> existing.getStatus() == AppointmentStatus.BOOKED)
                .anyMatch(existing -> sameLocation(existing.getShopLocation(), appointment.getShopLocation()));
    }

    private boolean canAccessRequest(TireRequest request) {
        return shopContextService.canAccessTenantResource(request.getShop(), request.getLocation());
    }

    private boolean belongsToCustomerShop(User customer, TireRequest request) {
        if (!Objects.equals(customer.getId(), request.getCustomerId())) {
            return false;
        }
        if (customer.getShop() == null) {
            return request.getShop() == null;
        }
        return request.getShop() != null && Objects.equals(customer.getShop().getId(), request.getShop().getId());
    }

    private Shop resolveRequestShop(User customer, CustomerVehicle vehicle, Appointment appointment) {
        if (appointment != null && appointment.getShop() != null) {
            return appointment.getShop();
        }
        if (vehicle.getShop() != null) {
            return vehicle.getShop();
        }
        return customer.getShop();
    }

    private ShopLocation resolveLocation(Long locationId, Shop shop, Appointment appointment, CustomerVehicle vehicle) {
        if (locationId != null) {
            return shopContextService.resolveAccessibleLocation(locationId, shop, true).orElse(null);
        }
        if (appointment != null && appointment.getShopLocation() != null) {
            return appointment.getShopLocation();
        }
        return vehicle.getShopLocation();
    }

    private TireRequestSource sourceFor(User user) {
        if (user.getRole() == UserRole.EMPLOYEE) {
            return TireRequestSource.EMPLOYEE;
        }
        return TireRequestSource.ADMIN;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void notifyStaff(TireRequest request, String title, String message, String type) {
        saveStaffNotification(request, title, message, type, UserRole.OWNER);
        saveStaffNotification(request, title, message, type, UserRole.ADMIN);
        saveStaffNotification(request, title, message, type, UserRole.EMPLOYEE);
    }

    private void saveStaffNotification(TireRequest request, String title, String message, String type, UserRole role) {
        AppNotification notification = new AppNotification();
        notification.setRecipientRole(role);
        notification.setShop(request.getShop());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetTab("Appointments");
        appNotificationRepository.save(notification);
    }

    private void notifyCustomer(TireRequest request, String title, String message) {
        if (request.getCustomer() == null) {
            return;
        }

        CustomerNotification notification = new CustomerNotification();
        notification.setCustomer(request.getCustomer());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("TIRE_REQUEST");
        customerNotificationRepository.save(notification);
    }

    private String buildStaffRequestMessage(TireRequest request) {
        String customerName = request.getCustomerName() == null ? "Customer" : request.getCustomerName();
        String locationName = request.getLocationName() == null ? "selected shop" : request.getLocationName();
        return customerName + " needs " + request.getRequestedSize() + " for " + locationName + ". Status: " + request.getStatus() + ".";
    }

    private String customerStatusTitle(TireRequestStatus status) {
        return switch (status) {
            case PENDING -> "Tire request pending";
            case SOURCING -> "Tire being sourced";
            case AVAILABLE -> "Tire available";
            case UNAVAILABLE -> "Tire unavailable";
            case FULFILLED -> "Tire request fulfilled";
            case DECLINED -> "Tire request declined";
            case CANCELLED -> "Tire request cancelled";
        };
    }

    private String customerStatusMessage(TireRequest request) {
        String response = request.getAdminResponse() == null || request.getAdminResponse().isBlank()
                ? ""
                : " " + request.getAdminResponse();
        return switch (request.getStatus()) {
            case PENDING -> "Your tire request is waiting for shop review." + response;
            case SOURCING -> "The shop is sourcing your tire." + response;
            case AVAILABLE -> "Your requested tire is available. Your appointment can now be confirmed." + response;
            case UNAVAILABLE -> "The shop could not source this tire at this time." + response;
            case FULFILLED -> "Your tire request has been fulfilled." + response;
            case DECLINED -> "The shop declined this tire request." + response;
            case CANCELLED -> "This tire request was cancelled." + response;
        };
    }

    private boolean sameLocation(ShopLocation first, ShopLocation second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }

        return first.getId() != null && first.getId().equals(second.getId());
    }

    private boolean sameShop(Shop first, Shop second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }

        return first.getId() != null && first.getId().equals(second.getId());
    }
}
