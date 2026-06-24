package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.EstimateItemRequest;
import com.aem.tiretrack.dto.EstimateRequest;
import com.aem.tiretrack.enums.EstimateStatus;
import com.aem.tiretrack.enums.InvoiceItemType;
import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.Estimate;
import com.aem.tiretrack.model.EstimateItem;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.InvoiceItem;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.AppNotificationRepository;
import com.aem.tiretrack.repository.CustomerNotificationRepository;
import com.aem.tiretrack.repository.EstimateRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class EstimateService {
    private final EstimateRepository estimateRepository;
    private final TireRepository tireRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final ShopContextService shopContextService;
    private final AppointmentService appointmentService;
    private final CustomerNotificationRepository customerNotificationRepository;
    private final AppNotificationRepository appNotificationRepository;

    public EstimateService(
            EstimateRepository estimateRepository,
            TireRepository tireRepository,
            UserRepository userRepository,
            InvoiceService invoiceService,
            ShopContextService shopContextService,
            AppointmentService appointmentService,
            CustomerNotificationRepository customerNotificationRepository,
            AppNotificationRepository appNotificationRepository) {
        this.estimateRepository = estimateRepository;
        this.tireRepository = tireRepository;
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
        this.shopContextService = shopContextService;
        this.appointmentService = appointmentService;
        this.customerNotificationRepository = customerNotificationRepository;
        this.appNotificationRepository = appNotificationRepository;
    }

    public List<Estimate> getAllEstimates() {
        return estimateRepository.findAll().stream()
                .filter(this::canAccessEstimate)
                .toList();
    }

    public Estimate getEstimateById(Long id) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estimate not found with id: " + id));
        ensureEstimateAccess(estimate);
        return estimate;
    }

    @Transactional
    public Estimate createEstimate(EstimateRequest request) {
        Estimate estimate = new Estimate();
        applyRequest(estimate, request);
        linkCustomerAccountIfPresent(estimate);
        Estimate savedEstimate = estimateRepository.save(estimate);
        ensureEstimateNumber(savedEstimate);
        notifyCustomerEstimateAvailable(savedEstimate);
        return savedEstimate;
    }

    @Transactional
    public Estimate updateEstimate(Long id, EstimateRequest request) {
        Estimate estimate = getEstimateById(id);
        if (estimate.getStatus() != EstimateStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft estimates can be edited.");
        }
        applyRequest(estimate, request);
        linkCustomerAccountIfPresent(estimate);
        return estimateRepository.save(estimate);
    }

    @Transactional
    public Estimate approveEstimate(Long id) {
        Estimate estimate = getEstimateById(id);
        requireConvertibleLifecycle(estimate, false);
        estimate.setStatus(EstimateStatus.APPROVED);
        if (estimate.getAppointmentId() == null) {
            Appointment appointment = createAppointmentFromEstimate(estimate);
            estimate.setAppointmentId(appointment.getId());
        }
        return estimateRepository.save(estimate);
    }

    @Transactional
    public Estimate sendEstimate(Long id) {
        Estimate estimate = getEstimateById(id);
        if (estimate.getStatus() == EstimateStatus.CONVERTED) {
            throw new IllegalArgumentException("Converted estimates cannot be sent again.");
        }
        if (estimate.getStatus() == EstimateStatus.CANCELLED
                || estimate.getStatus() == EstimateStatus.DECLINED
                || estimate.getStatus() == EstimateStatus.REJECTED
                || estimate.getStatus() == EstimateStatus.EXPIRED) {
            throw new IllegalArgumentException("Cancelled, rejected, declined, or expired estimates cannot be sent.");
        }

        estimate.setStatus(EstimateStatus.SENT);
        Estimate savedEstimate = estimateRepository.save(estimate);
        notifyEstimateSent(savedEstimate);
        return savedEstimate;
    }

    @Transactional
    public Estimate declineEstimate(Long id) {
        Estimate estimate = getEstimateById(id);
        if (estimate.getStatus() == EstimateStatus.CONVERTED) {
            throw new IllegalArgumentException("Converted estimates cannot be declined.");
        }
        estimate.setStatus(EstimateStatus.REJECTED);
        return estimateRepository.save(estimate);
    }

    @Transactional
    public Estimate cancelEstimate(Long id) {
        Estimate estimate = getEstimateById(id);
        if (estimate.getStatus() == EstimateStatus.CONVERTED) {
            throw new IllegalArgumentException("Converted estimates cannot be cancelled.");
        }
        estimate.setStatus(EstimateStatus.CANCELLED);
        return estimateRepository.save(estimate);
    }

    @Transactional
    public Invoice convertToInvoice(Long id) {
        return convertToInvoice(id, null);
    }

    @Transactional
    public Invoice convertToInvoice(Long id, Invoice invoiceDraft) {
        Estimate estimate = getEstimateById(id);
        requireConvertibleLifecycle(estimate, true);

        Invoice invoice = buildInvoiceFromEstimate(estimate, invoiceDraft);
        Invoice savedInvoice = invoiceService.saveInvoice(invoice);
        estimate.setConvertedInvoiceId(savedInvoice.getId());
        estimate.setStatus(EstimateStatus.CONVERTED);
        estimateRepository.save(estimate);
        return savedInvoice;
    }

    private Invoice buildInvoiceFromEstimate(Estimate estimate, Invoice invoiceDraft) {
        Invoice invoice = invoiceDraft == null ? new Invoice() : invoiceDraft;

        if (invoice.getCustomerId() == null && estimate.getCustomer() != null) {
            invoice.setCustomerId(estimate.getCustomer().getId());
        }
        invoice.setCustomerName(firstNonBlank(invoice.getCustomerName(), estimate.getCustomerName()));
        invoice.setPhone(firstNonBlank(invoice.getPhone(), estimate.getPhone()));
        invoice.setVehicle(firstNonBlank(invoice.getVehicle(), estimate.getVehicle()));
        if (invoice.getTaxRate() == null) {
            invoice.setTaxRate(estimate.getTaxRate());
        }
        invoice.setStatus(firstNonBlank(invoice.getStatus(), "UNPAID"));
        invoice.setPaymentMethod(firstNonBlank(invoice.getPaymentMethod(), "Manual"));
        if (invoice.getShop() == null) {
            invoice.setShop(estimate.getShop());
        }
        if (invoice.getShopLocation() == null && invoice.getRequestedLocationId() == null) {
            invoice.setShopLocation(estimate.getShopLocation());
        }

        if (invoice.getItems().isEmpty()) {
            for (EstimateItem estimateItem : estimate.getItems()) {
                invoice.addItem(toInvoiceItem(estimateItem));
            }
        }

        return invoice;
    }

    private void applyRequest(Estimate estimate, EstimateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Estimate details are required.");
        }

        if (request.getCustomerId() != null) {
            User customer = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + request.getCustomerId()));
            if (customer.getRole() != UserRole.CUSTOMER) {
                throw new IllegalArgumentException("Selected user is not a customer.");
            }
            if (!shopContextService.canAccessTenantUser(customer)) {
                throw new AccessDeniedException("You do not have permission to access this resource.");
            }
            estimate.setCustomer(customer);
            estimate.setCustomerName(customer.getFullName());
            estimate.setPhone(customer.getPhone());
            estimate.setEmail(customer.getEmail());
        }

        if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            estimate.setCustomerName(request.getCustomerName().trim());
        }
        if (request.getPhone() != null) {
            estimate.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            estimate.setEmail(request.getEmail());
        }
        estimate.setVehicle(request.getVehicle());
        estimate.setNotes(request.getNotes());
        estimate.setValidUntil(request.getValidUntil());
        estimate.setTaxRate(normalizeTaxRate(request.getTaxRate()));

        if (request.getLocationId() != null) {
            ShopLocation requestedLocation = shopContextService.resolveAccessibleLocation(
                    request.getLocationId(),
                    estimate.getShop(),
                    true).orElse(null);
            if (requestedLocation != null) {
                estimate.setShopLocation(requestedLocation);
                if (estimate.getShop() == null) {
                    estimate.setShop(requestedLocation.getShop());
                }
            }
        }

        if (estimate.getShop() == null) {
            shopContextService.getCurrentTenantShop().ifPresent(estimate::setShop);
        }
        if (estimate.getShopLocation() == null) {
            shopContextService.getCurrentTenantLocation()
                    .filter(location -> estimate.getShop() == null
                            || (location.getShop() != null && location.getShop().getId().equals(estimate.getShop().getId())))
                    .ifPresent(estimate::setShopLocation);
        }

        if (estimate.getCustomerName() == null || estimate.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (estimate.getPhone() == null || estimate.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone is required to convert estimates into invoices.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one estimate line item is required.");
        }

        estimate.getItems().clear();
        request.getItems().forEach(itemRequest -> estimate.addItem(toEstimateItem(itemRequest)));
        recalculate(estimate);
    }

    private EstimateItem toEstimateItem(EstimateItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Estimate item details are required.");
        }

        EstimateItem item = new EstimateItem();
        item.setItemType(request.getItemType() == null ? InvoiceItemType.SERVICE : request.getItemType());
        item.setTireId(request.getTireId());
        item.setQuantity(request.getQuantity() == null ? 1 : request.getQuantity());
        item.setUnitPrice(amount(request.getUnitPrice()));

        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Estimate item quantity must be at least 1.");
        }

        if (item.getItemType() == InvoiceItemType.TIRE) {
            if (item.getTireId() == null) {
                throw new IllegalArgumentException("Tire estimate items require a tire.");
            }
            Tire tire = tireRepository.findById(item.getTireId())
                    .orElseThrow(() -> new IllegalArgumentException("Tire not found with id: " + item.getTireId()));
            if (!shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation())) {
                throw new AccessDeniedException("You do not have permission to access this resource.");
            }
            item.setItemName(firstNonBlank(request.getItemName(), tire.getBrand() + " " + tire.getTireSize()));
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
                item.setUnitPrice(amount(tire.getPrice()));
            }
        } else {
            item.setTireId(null);
            item.setItemName(firstNonBlank(request.getItemName(), "Service"));
        }

        item.setLineTotal(amount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        return item;
    }

    private InvoiceItem toInvoiceItem(EstimateItem estimateItem) {
        InvoiceItem item = new InvoiceItem();
        item.setItemType(estimateItem.getItemType());
        item.setTireId(estimateItem.getTireId());
        item.setItemName(estimateItem.getItemName());
        item.setQuantity(estimateItem.getQuantity());
        item.setUnitPrice(estimateItem.getUnitPrice());
        return item;
    }

    private void recalculate(Estimate estimate) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (EstimateItem item : estimate.getItems()) {
            item.setLineTotal(amount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
            subtotal = subtotal.add(item.getLineTotal());
        }

        BigDecimal taxRate = normalizeTaxRate(estimate.getTaxRate());
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        estimate.setSubtotal(amount(subtotal));
        estimate.setTaxRate(taxRate);
        estimate.setTaxAmount(taxAmount);
        estimate.setTotal(amount(subtotal.add(taxAmount)));
    }

    private void ensureEstimateNumber(Estimate estimate) {
        if (estimate.getEstimateNumber() != null && !estimate.getEstimateNumber().isBlank()) {
            return;
        }
        estimate.setEstimateNumber("EST-" + String.format("%06d", estimate.getId()));
        estimateRepository.save(estimate);
    }

    private void notifyEstimateSent(Estimate estimate) {
        User customer = resolveEstimateCustomer(estimate);
        String estimateLabel = firstNonBlank(estimate.getEstimateNumber(), "#" + estimate.getId());

        if (customer != null) {
            CustomerNotification notification = new CustomerNotification();
            notification.setCustomer(customer);
            notification.setType("ESTIMATE");
            notification.setTitle("Estimate ready");
            notification.setMessage("Estimate " + estimateLabel + " is ready for review. Total: $" + amount(estimate.getTotal()) + ".");
            customerNotificationRepository.save(notification);
        }

        saveEstimateSentNotification(estimate, estimateLabel, UserRole.OWNER);
        saveEstimateSentNotification(estimate, estimateLabel, UserRole.ADMIN);
        saveEstimateSentNotification(estimate, estimateLabel, UserRole.EMPLOYEE);
    }

    private void notifyCustomerEstimateAvailable(Estimate estimate) {
        User customer = resolveEstimateCustomer(estimate);
        if (customer == null) {
            return;
        }

        String estimateLabel = firstNonBlank(estimate.getEstimateNumber(), "#" + estimate.getId());
        CustomerNotification notification = new CustomerNotification();
        notification.setCustomer(customer);
        notification.setType("ESTIMATE");
        notification.setTitle("New estimate available");
        notification.setMessage("Estimate " + estimateLabel + " is now available in your account. Total: $" + amount(estimate.getTotal()) + ".");
        customerNotificationRepository.save(notification);
    }

    private void linkCustomerAccountIfPresent(Estimate estimate) {
        if (estimate.getCustomer() != null) {
            return;
        }

        User customer = resolveEstimateCustomer(estimate);
        if (customer != null) {
            estimate.setCustomer(customer);
        }
    }

    private void saveEstimateSentNotification(Estimate estimate, String estimateLabel, UserRole role) {
        AppNotification notification = new AppNotification();
        notification.setRecipientRole(role);
        notification.setShop(estimate.getShop());
        notification.setTitle("Estimate sent");
        notification.setMessage("Estimate " + estimateLabel + " was sent to " + estimate.getCustomerName() + ".");
        notification.setType("ESTIMATE");
        notification.setTargetTab("Estimates");
        appNotificationRepository.save(notification);
    }

    private User resolveEstimateCustomer(Estimate estimate) {
        if (estimate.getCustomer() != null) {
            return estimate.getCustomer();
        }

        if (estimate.getPhone() != null && !estimate.getPhone().isBlank()) {
            User customer = userRepository.findByPhone(estimate.getPhone())
                    .filter(user -> user.getRole() == UserRole.CUSTOMER)
                    .filter(shopContextService::canAccessTenantUser)
                    .orElse(null);
            if (customer != null) {
                return customer;
            }
        }

        if (estimate.getEmail() != null && !estimate.getEmail().isBlank()) {
            User customer = userRepository.findByEmail(estimate.getEmail())
                    .filter(user -> user.getRole() == UserRole.CUSTOMER)
                    .filter(shopContextService::canAccessTenantUser)
                    .orElse(null);
            if (customer != null) {
                return customer;
            }
        }

        if (estimate.getCustomerName() != null && !estimate.getCustomerName().isBlank()) {
            return userRepository.findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER).stream()
                    .filter(user -> user.getFullName() != null && user.getFullName().equalsIgnoreCase(estimate.getCustomerName()))
                    .filter(shopContextService::canAccessTenantUser)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    private void requireConvertibleLifecycle(Estimate estimate, boolean converting) {
        if (estimate.getStatus() == EstimateStatus.CONVERTED) {
            throw new IllegalArgumentException("This estimate has already been converted to invoice #" + estimate.getConvertedInvoiceId());
        }
        if (estimate.getStatus() == EstimateStatus.CANCELLED
                || estimate.getStatus() == EstimateStatus.DECLINED
                || estimate.getStatus() == EstimateStatus.REJECTED
                || estimate.getStatus() == EstimateStatus.EXPIRED) {
            throw new IllegalArgumentException("Cancelled, rejected, declined, or expired estimates cannot be converted.");
        }
        if (estimate.getValidUntil() != null && estimate.getValidUntil().isBefore(LocalDate.now())) {
            estimate.setStatus(EstimateStatus.EXPIRED);
            throw new IllegalArgumentException("This estimate is expired.");
        }
        if (converting && estimate.getStatus() != EstimateStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved estimates can be converted to invoices.");
        }
    }

    private void ensureEstimateAccess(Estimate estimate) {
        if (!canAccessEstimate(estimate)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    private boolean canAccessEstimate(Estimate estimate) {
        return shopContextService.canAccessTenantResource(estimate.getShop(), estimate.getShopLocation());
    }

    private Appointment createAppointmentFromEstimate(Estimate estimate) {
        Appointment appointment = new Appointment();
        EstimateItem firstTireItem = estimate.getItems().stream()
                .filter(item -> item.getItemType() == InvoiceItemType.TIRE && item.getTireId() != null)
                .findFirst()
                .orElse(null);

        appointment.setCustomerId(estimate.getCustomer() == null ? null : estimate.getCustomer().getId());
        appointment.setCustomerName(estimate.getCustomerName());
        appointment.setPhone(estimate.getPhone());
        appointment.setEmail(estimate.getEmail());
        appointment.setVehicle(estimate.getVehicle());
        appointment.setServiceType(firstTireItem == null ? ServiceType.REPAIR : ServiceType.INSTALLATION);
        appointment.setNotes(firstNonBlank(
                estimate.getNotes(),
                "Approved estimate " + firstNonBlank(estimate.getEstimateNumber(), "#" + estimate.getId())));
        appointment.setShop(estimate.getShop());
        appointment.setShopLocation(estimate.getShopLocation());

        if (firstTireItem != null) {
            Tire tire = tireRepository.findById(firstTireItem.getTireId())
                    .orElseThrow(() -> new IllegalArgumentException("Tire not found with id: " + firstTireItem.getTireId()));
            if (!shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation())) {
                throw new AccessDeniedException("You do not have permission to access this resource.");
            }
            appointment.setFrontTireId(tire.getId());
            appointment.setFrontQuantity(firstTireItem.getQuantity());
            appointment.setTireSize(tire.getTireSize());
        }

        appointment.setAppointmentDate(nextAvailableAppointmentDate(estimate));
        return appointmentService.saveAppointment(appointment);
    }

    private java.time.LocalDateTime nextAvailableAppointmentDate(Estimate estimate) {
        for (int offset = 1; offset <= 30; offset++) {
            LocalDate date = LocalDate.now().plusDays(offset);
            List<String> slots = appointmentService.getAvailableSlots(date, estimate.getLocationId());

            if (!slots.isEmpty()) {
                return date.atTime(LocalTime.parse(slots.get(0)));
            }
        }

        throw new IllegalArgumentException("No appointment slots are available in the next 30 days.");
    }

    private BigDecimal normalizeTaxRate(BigDecimal taxRate) {
        BigDecimal resolved = taxRate == null ? new BigDecimal("0.13") : taxRate;
        if (resolved.compareTo(BigDecimal.ONE) > 0) {
            resolved = resolved.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        }
        return resolved.max(BigDecimal.ZERO);
    }

    private BigDecimal amount(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String firstNonBlank(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first.trim();
    }
}
