package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.customer.CustomerAppointmentRequest;
import com.aem.tiretrack.dto.EstimateResponse;
import com.aem.tiretrack.dto.InvoiceStatusUpdateRequest;
import com.aem.tiretrack.dto.customer.CustomerInvoiceSummary;
import com.aem.tiretrack.dto.customer.CustomerNoticeRequest;
import com.aem.tiretrack.dto.customer.CustomerProfile;
import com.aem.tiretrack.dto.customer.CustomerPortalResponse;
import com.aem.tiretrack.dto.customer.CustomerSummary;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Estimate;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.CustomerNotificationRepository;
import com.aem.tiretrack.repository.CustomerVehicleRepository;
import com.aem.tiretrack.repository.EstimateRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class CustomerPortalService {
    private final UserRepository userRepository;
    private final CustomerVehicleRepository vehicleRepository;
    private final CustomerNotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final EstimateRepository estimateRepository;
    private final AppointmentService appointmentService;
    private final EstimateService estimateService;
    private final InvoiceService invoiceService;
    private final ShopContextService shopContextService;

    public CustomerPortalService(
            UserRepository userRepository,
            CustomerVehicleRepository vehicleRepository,
            CustomerNotificationRepository notificationRepository,
            AppointmentRepository appointmentRepository,
            InvoiceRepository invoiceRepository,
            EstimateRepository estimateRepository,
            AppointmentService appointmentService,
            EstimateService estimateService,
            InvoiceService invoiceService,
            ShopContextService shopContextService) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.estimateRepository = estimateRepository;
        this.appointmentService = appointmentService;
        this.estimateService = estimateService;
        this.invoiceService = invoiceService;
        this.shopContextService = shopContextService;
    }

    public CustomerPortalResponse portal() {
        User customer = currentCustomer();
        List<Appointment> appointments = appointmentRepository.findCustomerHistory(customer.getId(), customer.getPhone(), customer.getEmail()).stream()
                .filter(appointment -> customerCanAccessShop(customer, appointment.getShop()))
                .toList();
        List<Invoice> invoices = invoiceRepository.findCustomerHistory(customer.getId(), customer.getPhone(), customer.getFullName()).stream()
                .filter(invoice -> customerCanAccessShop(customer, invoice.getShop()))
                .toList();
        List<EstimateResponse> estimates = estimateRepository.findCustomerHistory(
                        customer.getId(),
                        blankToNull(customer.getPhone()),
                        blankToNull(customer.getEmail()),
                        blankToNull(customer.getFullName())).stream()
                .filter(estimate -> customerCanAccessShop(customer, estimate.getShop()))
                .map(EstimateResponse::new)
                .toList();
        return new CustomerPortalResponse(
                new CustomerProfile(customer),
                vehicleRepository.findByCustomerOrderByCreatedAtDesc(customer),
                appointments,
                invoices,
                estimates,
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
        appointment.setEmail(customer.getEmail());
        appointment.setPhone(customer.getPhone());
        appointment.setVehicle(vehicleLabel(vehicle));
        appointment.setTireSize(vehicleTireSize(vehicle));
        appointment.setAppointmentDate(request.getAppointmentDate().atTime(request.getAppointmentTime()));
        appointment.setServiceType(request.getServiceType());
        appointment.setNotes(request.getNotes());
        appointment.setShop(customer.getShop());

        return appointmentService.saveAppointment(appointment);
    }

    public List<CustomerSummary> adminSummaries() {
        List<User> customers = userRepository.findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER).stream()
                .filter(shopContextService::canAccessTenantUser)
                .toList();

        return customers.stream()
                .map(this::summary)
                .toList();
    }

    @Transactional
    public Invoice payInvoice(Long invoiceId) {
        User customer = currentCustomer();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        boolean ownsInvoice = customer.getId().equals(invoice.getCustomerId())
                || equalsIgnoreCase(customer.getPhone(), invoice.getPhone())
                || equalsIgnoreCase(customer.getFullName(), invoice.getCustomerName());
        boolean sameShop = customer.getShop() == null
                ? invoice.getShop() == null
                : invoice.getShop() != null && customer.getShop().getId().equals(invoice.getShop().getId());
        if (!ownsInvoice || !sameShop) {
            throw new RuntimeException("Invoice not found");
        }

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            return invoice;
        }

        InvoiceStatusUpdateRequest request = new InvoiceStatusUpdateRequest();
        request.setStatus("PAID");
        request.setPaymentMethod("Customer Portal");
        Invoice paidInvoice = invoiceService.updateInvoiceStatus(invoiceId, request);

        CustomerNotification notification = new CustomerNotification();
        notification.setCustomer(customer);
        notification.setType("PAYMENT");
        notification.setTitle("Payment received");
        notification.setMessage("Invoice #" + invoice.getId() + " was marked paid.");
        notificationRepository.save(notification);

        return paidInvoice;
    }

    @Transactional
    public EstimateResponse approveEstimate(Long estimateId) {
        User customer = currentCustomer();
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new RuntimeException("Estimate not found"));

        if (!ownsEstimate(customer, estimate) || !sameShop(customer, estimate)) {
            throw new RuntimeException("Estimate not found");
        }

        Estimate approvedEstimate = estimateService.approveEstimate(estimateId);

        CustomerNotification notification = new CustomerNotification();
        notification.setCustomer(customer);
        notification.setType("ESTIMATE");
        notification.setTitle("Estimate approved");
        notification.setMessage("Estimate " + approvedEstimate.getEstimateNumber() + " was approved and an appointment was created.");
        notificationRepository.save(notification);

        return new EstimateResponse(approvedEstimate);
    }

    public CustomerNotification sendNotice(Long customerId, CustomerNoticeRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        ensureCustomerAccess(customer);

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
        List<Appointment> appointments = appointmentRepository.findCustomerHistory(customer.getId(), customer.getPhone(), customer.getEmail()).stream()
                .filter(appointment -> shopContextService.canAccessTenantResource(appointment.getShop(), appointment.getShopLocation()))
                .toList();
        List<Invoice> invoices = invoiceRepository.findCustomerHistory(customer.getId(), customer.getPhone(), customer.getFullName()).stream()
                .filter(invoice -> shopContextService.canAccessTenantResource(invoice.getShop(), invoice.getShopLocation()))
                .toList();
        List<CustomerVehicle> vehicles = vehicleRepository.findByCustomerOrderByCreatedAtDesc(customer);
        BigDecimal totalSpent = invoices.stream()
                .map(this::collectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Invoice> unpaidInvoices = invoices.stream()
                .filter(this::isCustomerBalanceInvoice)
                .toList();
        BigDecimal outstandingBalance = unpaidInvoices.stream()
                .map(this::balanceDue)
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
                nextAppointment != null,
                vehicles,
                unpaidInvoices.stream().map(CustomerInvoiceSummary::new).toList()
        );
    }

    private boolean isCustomerBalanceInvoice(Invoice invoice) {
        String status = invoice.getStatus() == null ? "UNPAID" : invoice.getStatus().trim();
        return !"PAID".equalsIgnoreCase(status) && !"VOID".equalsIgnoreCase(status);
    }

    private BigDecimal collectedAmount(Invoice invoice) {
        if (invoice.getAmountPaid() != null && invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            return invoice.getAmountPaid();
        }

        return "PAID".equalsIgnoreCase(invoice.getStatus())
                ? invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal()
                : BigDecimal.ZERO;
    }

    private BigDecimal balanceDue(Invoice invoice) {
        if (invoice.getBalanceDue() != null && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            return invoice.getBalanceDue();
        }

        BigDecimal total = invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal();
        return total.subtract(collectedAmount(invoice)).max(BigDecimal.ZERO);
    }

    private boolean ownsEstimate(User customer, Estimate estimate) {
        return (estimate.getCustomer() != null && Objects.equals(customer.getId(), estimate.getCustomer().getId()))
                || equalsIgnoreCase(customer.getPhone(), estimate.getPhone())
                || equalsIgnoreCase(customer.getEmail(), estimate.getEmail())
                || equalsIgnoreCase(customer.getFullName(), estimate.getCustomerName());
    }

    private boolean sameShop(User customer, Estimate estimate) {
        return customerCanAccessShop(customer, estimate.getShop());
    }

    private boolean customerCanAccessShop(User customer, com.aem.tiretrack.model.Shop recordShop) {
        if (customer.getShop() == null) {
            return recordShop == null;
        }

        return recordShop != null && Objects.equals(customer.getShop().getId(), recordShop.getId());
    }

    private boolean equalsIgnoreCase(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private User currentCustomer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    private void ensureCustomerAccess(User customer) {
        if (!shopContextService.canAccessTenantUser(customer)) {
            throw new RuntimeException("Customer not found");
        }
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
