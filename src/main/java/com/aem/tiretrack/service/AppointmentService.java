package com.aem.tiretrack.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.AppNotificationRepository;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class AppointmentService {
    private static final LocalTime SLOT_START = LocalTime.of(9, 0);
    private static final LocalTime SLOT_END = LocalTime.of(17, 0);
    private static final int SLOT_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final TireRepository tireRepository;
    private final UserRepository userRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final AuditLogService auditLogService;
    private final ShopContextService shopContextService;

    public AppointmentService(AppointmentRepository appointmentRepository, TireRepository tireRepository, UserRepository userRepository, AppNotificationRepository appNotificationRepository, AuditLogService auditLogService, ShopContextService shopContextService) {
        this.appointmentRepository = appointmentRepository;
        this.tireRepository = tireRepository;
        this.userRepository = userRepository;
        this.appNotificationRepository = appNotificationRepository;
        this.auditLogService = auditLogService;
        this.shopContextService = shopContextService;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .filter(this::canAccessAppointment)
                .toList();
    }

    public Appointment getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        ensureAppointmentAccess(appointment);
        return appointment;
    }

    public List<String> getAvailableSlots(LocalDate date) {
        return getAvailableSlots(date, null);
    }

    public List<String> getAvailableSlots(LocalDate date, Long locationId) {
        ShopLocation selectedLocation = shopContextService.resolveAccessibleLocation(locationId, null, true).orElse(null);
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        List<Appointment> appointmentsForDay = appointmentRepository.findByAppointmentDateBetween(dayStart, dayEnd).stream()
                .filter(this::canAccessAppointment)
                .filter(appointment -> selectedLocation == null || sameLocation(appointment.getShopLocation(), selectedLocation))
                .toList();

        Set<LocalTime> bookedTimes = appointmentsForDay.stream()
                .filter(this::shouldReserve)
                .map(appointment -> appointment.getAppointmentDate().toLocalTime())
                .collect(Collectors.toSet());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today)) {
            return List.of();
        }

        List<String> availableSlots = new ArrayList<>();
        for (LocalTime slot = SLOT_START; !slot.isAfter(SLOT_END.minusMinutes(SLOT_MINUTES)); slot = slot.plusMinutes(SLOT_MINUTES)) {
            boolean isPastTodaySlot = date.isEqual(today) && !slot.isAfter(now);

            if (!isPastTodaySlot && !bookedTimes.contains(slot)) {
                availableSlots.add(slot.format(formatter));
            }
        }

        return availableSlots;
    }

    @Transactional
    public Appointment saveAppointment(Appointment appointment) {
        assignCurrentTenantContextIfMissing(appointment);
        normalizeReminderFields(appointment);
        validateAppointmentTime(appointment, null);
        linkCustomer(appointment);
        reserveAppointmentTires(appointment);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        auditLogService.record("BOOKED", "Appointment", savedAppointment.getId(), "Booked appointment for " + savedAppointment.getCustomerName());
        return savedAppointment;
    }

    @Transactional
    public Appointment updateAppointment(Long id, Appointment updatedAppointment) {

        Appointment existingAppointment = getAppointmentById(id);
        assignCurrentTenantContextIfMissing(updatedAppointment);
        normalizeReminderFields(updatedAppointment);
        validateAppointmentTime(updatedAppointment, id);
        releaseAppointmentTires(existingAppointment);

        existingAppointment.setCustomerName(updatedAppointment.getCustomerName());
        existingAppointment.setPhone(updatedAppointment.getPhone());
        existingAppointment.setEmail(updatedAppointment.getEmail());
        existingAppointment.setVehicle(updatedAppointment.getVehicle());
        existingAppointment.setTireSize(updatedAppointment.getTireSize());
        existingAppointment.setFrontTireId(updatedAppointment.getFrontTireId());
        existingAppointment.setFrontQuantity(updatedAppointment.getFrontQuantity());
        existingAppointment.setRearTireId(updatedAppointment.getRearTireId());
        existingAppointment.setRearQuantity(updatedAppointment.getRearQuantity());
        existingAppointment.setAppointmentDate(updatedAppointment.getAppointmentDate());
        existingAppointment.setServiceType(updatedAppointment.getServiceType());
        existingAppointment.setNotes(updatedAppointment.getNotes());
        existingAppointment.setReminderStatus(updatedAppointment.getReminderStatus());
        existingAppointment.setReminderAt(updatedAppointment.getReminderAt());
        existingAppointment.setConfirmationStatus(updatedAppointment.getConfirmationStatus());
        existingAppointment.setCancelReason(updatedAppointment.getCancelReason());
        if (existingAppointment.getShop() == null && updatedAppointment.getShop() != null) {
            existingAppointment.setShop(updatedAppointment.getShop());
        }
        if (existingAppointment.getShopLocation() == null && updatedAppointment.getShopLocation() != null) {
            existingAppointment.setShopLocation(updatedAppointment.getShopLocation());
        }

        if (updatedAppointment.getStatus() != null) {
        existingAppointment.setStatus(updatedAppointment.getStatus());
        }

        linkCustomer(existingAppointment);
        reserveAppointmentTires(existingAppointment);
        Appointment savedAppointment = appointmentRepository.save(existingAppointment);
        auditLogService.record("UPDATED", "Appointment", savedAppointment.getId(), "Updated appointment for " + savedAppointment.getCustomerName());
        return savedAppointment;
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        releaseAppointmentTires(appointment);
        appointmentRepository.delete(appointment);
        auditLogService.record("CANCELLED", "Appointment", id, "Cancelled appointment for " + appointment.getCustomerName(), getCurrentUsername());
    }

    @Scheduled(fixedDelayString = "${tiretrack.reminders.fixed-delay-ms:60000}")
    @Transactional
    public void sendDueReminders() {
        List<Appointment> dueReminders = appointmentRepository.findByReminderStatusAndReminderAtLessThanEqual(
                "SCHEDULED",
                LocalDateTime.now());

        for (Appointment appointment : dueReminders) {
            if (!shouldSendReminder(appointment)) {
                appointment.setReminderStatus("NOT_SET");
                continue;
            }

            saveAppointmentReminderNotification(appointment, UserRole.OWNER);
            saveAppointmentReminderNotification(appointment, UserRole.ADMIN);
            appointment.setReminderStatus("SENT");
        }
    }


    private void reserveAppointmentTires(Appointment appointment) {
        if (!shouldReserve(appointment)) {
            return;
        }

        changeReservation(appointment.getFrontTireId(), appointment.getFrontQuantity(), 1);
        changeReservation(appointment.getRearTireId(), appointment.getRearQuantity(), 1);
    }

    private void releaseAppointmentTires(Appointment appointment) {
        if (!shouldReserve(appointment)) {
            return;
        }

        changeReservation(appointment.getFrontTireId(), appointment.getFrontQuantity(), -1);
        changeReservation(appointment.getRearTireId(), appointment.getRearQuantity(), -1);
    }

    private boolean shouldReserve(Appointment appointment) {
        return appointment.getStatus() == null || appointment.getStatus() == AppointmentStatus.BOOKED;
    }

    private boolean shouldSendReminder(Appointment appointment) {
        return appointment.getReminderAt() != null && shouldReserve(appointment);
    }

    private void saveAppointmentReminderNotification(Appointment appointment, UserRole role) {
        AppNotification notification = new AppNotification();
        notification.setRecipientRole(role);
        notification.setShop(appointment.getShop());
        notification.setTitle("Appointment reminder");
        notification.setMessage(buildReminderMessage(appointment));
        notification.setType("REMINDER");
        notification.setTargetTab("Appointments");
        appNotificationRepository.save(notification);
    }

    private String buildReminderMessage(Appointment appointment) {
        String customerName = appointment.getCustomerName() == null || appointment.getCustomerName().isBlank()
                ? "Customer"
                : appointment.getCustomerName();
        String appointmentTime = appointment.getAppointmentDate() == null
                ? "soon"
                : appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));

        return customerName + " has an appointment at " + appointmentTime + ".";
    }

    private void normalizeReminderFields(Appointment appointment) {
        if (appointment.getReminderAt() == null) {
            appointment.setReminderStatus("NOT_SET");
        } else if (appointment.getReminderStatus() == null
                || appointment.getReminderStatus().isBlank()
                || "NOT_SET".equalsIgnoreCase(appointment.getReminderStatus())) {
            appointment.setReminderStatus("SCHEDULED");
        } else {
            appointment.setReminderStatus(appointment.getReminderStatus().trim().toUpperCase(Locale.ROOT));
        }

        if (appointment.getConfirmationStatus() == null || appointment.getConfirmationStatus().isBlank()) {
            appointment.setConfirmationStatus("PENDING");
        } else {
            appointment.setConfirmationStatus(appointment.getConfirmationStatus().trim().toUpperCase(Locale.ROOT));
        }
    }

    private void validateAppointmentTime(Appointment appointment, Long ignoredAppointmentId) {
        if (appointment.getAppointmentDate() == null || !shouldReserve(appointment)) {
            return;
        }

        LocalDateTime appointmentDate = appointment.getAppointmentDate();
        LocalTime appointmentTime = appointmentDate.toLocalTime();

        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Choose a future appointment time");
        }

        if (appointmentTime.isBefore(SLOT_START) || appointmentTime.isAfter(SLOT_END.minusMinutes(SLOT_MINUTES))) {
            throw new RuntimeException("Choose an appointment during business hours");
        }

        if (appointmentTime.getMinute() % SLOT_MINUTES != 0 || appointmentTime.getSecond() != 0 || appointmentTime.getNano() != 0) {
            throw new RuntimeException("Choose a valid appointment slot");
        }

        List<Appointment> appointmentsAtTime = appointmentRepository.findByAppointmentDate(appointment.getAppointmentDate()).stream()
                .filter(this::canAccessAppointment)
                .toList();

        boolean hasConflict = appointmentsAtTime.stream()
                .filter(existingAppointment -> ignoredAppointmentId == null || !ignoredAppointmentId.equals(existingAppointment.getId()))
                .filter(existingAppointment -> sameLocation(existingAppointment.getShopLocation(), appointment.getShopLocation()))
                .anyMatch(this::shouldReserve);

        if (hasConflict) {
            throw new RuntimeException("That appointment time is already booked");
        }
    }

    private void changeReservation(Long tireId, int quantity, int direction) {
        if (tireId == null || quantity <= 0) {
            return;
        }

        Tire tire = tireRepository.findById(tireId)
                .orElseThrow(() -> new RuntimeException("Tire not found"));

        if (!shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        if (direction > 0 && tire.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Not enough available tire stock to reserve");
        }

        int nextReservedQuantity = tire.getReservedQuantity() + (quantity * direction);

        if (nextReservedQuantity < 0) {
            nextReservedQuantity = 0;
        }

        tire.setReservedQuantity(nextReservedQuantity);
    }

    private void linkCustomer(Appointment appointment) {
        if (appointment.getCustomerId() != null) {
            userRepository.findById(appointment.getCustomerId()).ifPresent(user -> assignCustomerShopIfMissing(user, appointment));
            return;
        }

        if (appointment.getEmail() != null && !appointment.getEmail().isBlank()) {
            userRepository.findByEmail(appointment.getEmail()).ifPresent(user -> {
                appointment.setCustomerId(user.getId());
                appointment.setCustomerName(user.getFullName());
                appointment.setPhone(user.getPhone());
                assignCustomerShopIfMissing(user, appointment);
            });
        }

        if (appointment.getCustomerId() == null && appointment.getPhone() != null) {
            userRepository.findByPhone(appointment.getPhone()).ifPresent(user -> {
                appointment.setCustomerId(user.getId());
                appointment.setCustomerName(user.getFullName());
                appointment.setEmail(user.getEmail());
                assignCustomerShopIfMissing(user, appointment);
            });
        }
    }

    private void assignCurrentTenantContextIfMissing(Appointment appointment) {
        if (appointment.getRequestedLocationId() != null) {
            ShopLocation requestedLocation = shopContextService.resolveAccessibleLocation(
                    appointment.getRequestedLocationId(),
                    appointment.getShop(),
                    true).orElse(null);
            if (requestedLocation != null) {
                appointment.setShopLocation(requestedLocation);
                if (appointment.getShop() == null) {
                    appointment.setShop(requestedLocation.getShop());
                }
            }
        }
        if (appointment.getShop() == null) {
            shopContextService.getCurrentTenantShop().ifPresent(appointment::setShop);
        }
        if (appointment.getShopLocation() == null) {
            shopContextService.getCurrentTenantLocation()
                    .filter(location -> appointment.getShop() == null
                            || (location.getShop() != null && location.getShop().getId().equals(appointment.getShop().getId())))
                    .ifPresent(appointment::setShopLocation);
        }
    }

    private void assignCustomerShopIfMissing(User user, Appointment appointment) {
        Shop shop = appointment.getShop();

        if (shop == null) {
            return;
        }

        if (user.getShop() == null) {
            user.setShop(shop);
            if (appointment.getShopLocation() != null) {
                user.setShopLocation(appointment.getShopLocation());
            }
            userRepository.save(user);
            return;
        }

        if (!shop.getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Customer belongs to another shop");
        }

        if (user.getShopLocation() == null && appointment.getShopLocation() != null) {
            user.setShopLocation(appointment.getShopLocation());
            userRepository.save(user);
        }
    }

    private boolean sameLocation(ShopLocation first, ShopLocation second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }

        return first.getId() != null && first.getId().equals(second.getId());
    }

    private void ensureAppointmentAccess(Appointment appointment) {
        if (!canAccessAppointment(appointment)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    private boolean canAccessAppointment(Appointment appointment) {
        return shopContextService.canAccessTenantResource(appointment.getShop(), appointment.getShopLocation());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getName() != null
                ? authentication.getName()
                : "system";
    }
}
