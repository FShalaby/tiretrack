package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.customer.CustomerAppointmentRequest;
import com.aem.tiretrack.dto.customer.CustomerNoticeRequest;
import com.aem.tiretrack.dto.customer.CustomerPortalResponse;
import com.aem.tiretrack.dto.customer.CustomerSummary;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.CustomerNotificationRepository;
import com.aem.tiretrack.repository.CustomerVehicleRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class CustomerPortalService {
    private final UserRepository userRepository;
    private final CustomerVehicleRepository vehicleRepository;
    private final CustomerNotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final AppointmentService appointmentService;

    public CustomerPortalService(UserRepository userRepository, CustomerVehicleRepository vehicleRepository, CustomerNotificationRepository notificationRepository, AppointmentRepository appointmentRepository, InvoiceRepository invoiceRepository, AppointmentService appointmentService) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.appointmentService = appointmentService;
    }

    public CustomerPortalResponse portal() {
        User customer = currentCustomer();
        return new CustomerPortalResponse(
                customer,
                vehicleRepository.findByCustomerOrderByCreatedAtDesc(customer),
                appointmentRepository.findCustomerHistory(customer.getId(), customer.getPhone()),
                invoiceRepository.findCustomerHistory(customer.getId(), customer.getPhone()),
                notificationRepository.findByCustomerOrderByCreatedAtDesc(customer)
        );
    }

    public CustomerVehicle saveVehicle(CustomerVehicle vehicle) {
        User customer = currentCustomer();
        vehicle.setCustomer(customer);
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        User customer = currentCustomer();
        CustomerVehicle vehicle = vehicleRepository.findByIdAndCustomer(id, customer)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        vehicleRepository.delete(vehicle);
    }

    @Transactional
    public Appointment bookAppointment(CustomerAppointmentRequest request) {
        User customer = currentCustomer();
        CustomerVehicle vehicle = vehicleRepository.findByIdAndCustomer(request.getVehicleId(), customer)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Appointment appointment = new Appointment();
        appointment.setCustomerId(customer.getId());
        appointment.setCustomerName(customer.getFullName());
        appointment.setPhone(customer.getPhone());
        appointment.setVehicle(vehicleLabel(vehicle));
        appointment.setTireSize(vehicleTireSize(vehicle));
        appointment.setAppointmentDate(request.getAppointmentDate().atTime(request.getAppointmentTime()));
        appointment.setServiceType(request.getServiceType());
        appointment.setNotes(request.getNotes());

        return appointmentService.saveAppointment(appointment);
    }

    public List<CustomerSummary> adminSummaries() {
        return userRepository.findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER).stream()
                .map(this::summary)
                .toList();
    }

    @Transactional
    public Invoice payInvoice(Long invoiceId) {
        User customer = currentCustomer();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        boolean ownsInvoice = customer.getId().equals(invoice.getCustomerId()) || customer.getPhone().equals(invoice.getPhone());
        if (!ownsInvoice) {
            throw new RuntimeException("Invoice not found");
        }

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            return invoice;
        }

        invoice.setStatus("PAID");
        invoice.setPaymentMethod("Customer Portal");
        invoice.setPaidAt(LocalDateTime.now());

        CustomerNotification notification = new CustomerNotification();
        notification.setCustomer(customer);
        notification.setType("PAYMENT");
        notification.setTitle("Payment received");
        notification.setMessage("Invoice #" + invoice.getId() + " was marked paid.");
        notificationRepository.save(notification);

        return invoiceRepository.save(invoice);
    }

    public CustomerNotification sendNotice(Long customerId, CustomerNoticeRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerNotification notification = new CustomerNotification();
        notification.setCustomer(customer);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType() == null || request.getType().isBlank() ? "NOTICE" : request.getType());
        return notificationRepository.save(notification);
    }

    public CustomerNotification markNotificationRead(Long id) {
        User customer = currentCustomer();
        CustomerNotification notification = notificationRepository.findByIdAndCustomer(id, customer)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    private CustomerSummary summary(User customer) {
        List<Appointment> appointments = appointmentRepository.findCustomerHistory(customer.getId(), customer.getPhone());
        List<Invoice> invoices = invoiceRepository.findCustomerHistory(customer.getId(), customer.getPhone());
        BigDecimal totalSpent = invoices.stream()
                .filter(invoice -> "PAID".equalsIgnoreCase(invoice.getStatus()))
                .map(invoice -> invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Invoice> unpaidInvoices = invoices.stream()
                .filter(invoice -> !"PAID".equalsIgnoreCase(invoice.getStatus()))
                .toList();
        BigDecimal outstandingBalance = unpaidInvoices.stream()
                .map(invoice -> invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Invoice nextUnpaidInvoice = unpaidInvoices.stream()
                .min(Comparator.comparing(invoice -> invoice.getDueDate() == null ? LocalDate.MAX : invoice.getDueDate()))
                .orElse(null);
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3);
        LocalDate nextDueDate = nextUnpaidInvoice == null ? null : nextUnpaidInvoice.getDueDate();
        boolean hasOverdueBalance = nextDueDate != null && nextDueDate.isBefore(today);
        boolean hasBalanceDueSoon = nextDueDate != null && !nextDueDate.isBefore(today) && !nextDueDate.isAfter(soon);
        Appointment nextAppointment = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate() != null)
                .filter(appointment -> appointment.getAppointmentDate().isAfter(LocalDateTime.now()))
                .filter(appointment -> appointment.getStatus() == null || appointment.getStatus() == AppointmentStatus.BOOKED)
                .min(Comparator.comparing(Appointment::getAppointmentDate))
                .orElse(null);

        return new CustomerSummary(
                customer,
                vehicleRepository.countByCustomer(customer),
                appointments.size(),
                invoices.size(),
                totalSpent,
                outstandingBalance,
                nextUnpaidInvoice == null ? null : nextUnpaidInvoice.getId(),
                nextDueDate,
                hasOverdueBalance,
                hasBalanceDueSoon,
                nextAppointment == null ? null : nextAppointment.getId(),
                nextAppointment == null ? null : nextAppointment.getAppointmentDate(),
                nextAppointment == null ? null : nextAppointment.getVehicle(),
                nextAppointment != null
        );
    }

    private User currentCustomer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    private String vehicleLabel(CustomerVehicle vehicle) {
        return String.join(" ", List.of(
                vehicle.getYear() == null ? "" : vehicle.getYear(),
                vehicle.getMake() == null ? "" : vehicle.getMake(),
                vehicle.getModel() == null ? "" : vehicle.getModel()
        )).trim();
    }

    private String vehicleTireSize(CustomerVehicle vehicle) {
        if ("staggered".equalsIgnoreCase(vehicle.getTireSetup())) {
            return "Front: " + safeText(vehicle.getFrontTireSize()) + " / Rear: " + safeText(vehicle.getRearTireSize());
        }

        return vehicle.getTireSize();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
