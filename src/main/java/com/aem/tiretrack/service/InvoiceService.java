package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.InvoiceItemType;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.InvoiceItem;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final TireRepository tireRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AccountingService accountingService;

    public InvoiceService(InvoiceRepository invoiceRepository, TireRepository tireRepository, AppointmentRepository appointmentRepository, UserRepository userRepository, AuditLogService auditLogService, AccountingService accountingService) {
        this.invoiceRepository = invoiceRepository;
        this.tireRepository = tireRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.accountingService = accountingService;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    @Transactional
    public Invoice saveInvoice(Invoice invoice) {
        try {
            prepareInvoice(invoice);
            linkCustomerByPhone(invoice);
            prepareInvoiceLifecycle(invoice);
            Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
            accountingService.recordInvoiceIssued(savedInvoice);
            if ("PAID".equalsIgnoreCase(savedInvoice.getStatus())) {
                accountingService.recordInvoicePayment(savedInvoice);
            }
            auditLogService.record("CREATED", "Invoice", savedInvoice.getId(), "Created invoice for " + savedInvoice.getCustomerName(), getCurrentUsername());
            return savedInvoice;
        } catch (RuntimeException exception) {
            throw new RuntimeException(rootMessage(exception), exception);
        }
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);
        invoiceRepository.deleteById(id);
        auditLogService.record("DELETED", "Invoice", id, "Deleted invoice for " + invoice.getCustomerName(), getCurrentUsername());
    }

    @Transactional
    public Invoice updateInvoiceStatus(Long id, String status) {
        Invoice invoice = getInvoiceById(id);
        String nextStatus = status == null ? "UNPAID" : status.trim().toUpperCase();
        invoice.setStatus(nextStatus);

        if ("PAID".equalsIgnoreCase(nextStatus) && invoice.getAppointmentId() != null) {
            invoice.setPaidAt(LocalDateTime.now());
            Appointment appointment = appointmentRepository.findById(invoice.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            releaseRemainingAppointmentReservations(appointment, new HashMap<>());
            appointment.setStatus(AppointmentStatus.COMPLETED);
        }

        if ("PAID".equalsIgnoreCase(nextStatus)) {
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setPaymentMethod(invoice.getPaymentMethod() == null ? "Manual" : invoice.getPaymentMethod());
        } else {
            invoice.setPaidAt(null);
            if (!"VOID".equalsIgnoreCase(nextStatus) && invoice.getDueDate() == null) {
                invoice.setDueDate(LocalDate.now().plusDays(14));
            }
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        if ("PAID".equalsIgnoreCase(nextStatus)) {
            accountingService.recordInvoicePayment(savedInvoice);
        }
        auditLogService.record("STATUS_CHANGED", "Invoice", savedInvoice.getId(), "Invoice #" + savedInvoice.getId() + " marked " + nextStatus, getCurrentUsername());
        return savedInvoice;
    }

    private void prepareInvoice(Invoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO;
        Map<Long, Integer> consumedAppointmentReservations = new HashMap<>();
        Appointment appointment = invoice.getAppointmentId() == null
                ? null
                : appointmentRepository.findById(invoice.getAppointmentId())
                        .orElseThrow(() -> new RuntimeException("Appointment not found"));

        for (InvoiceItem item : invoice.getItems()) {
            item.setInvoice(invoice);

            if (item.getItemType() == InvoiceItemType.TIRE) {
                handleTireItem(item, appointment, consumedAppointmentReservations);
            }

            BigDecimal unitPrice = item.getUnitPrice() == null
                    ? BigDecimal.ZERO
                    : item.getUnitPrice();

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            item.setUnitPrice(unitPrice);
            item.setTotalPrice(itemTotal);

            subtotal = subtotal.add(itemTotal);
        }

        if (appointment != null && "PAID".equalsIgnoreCase(invoice.getStatus())) {
            releaseRemainingAppointmentReservations(appointment, consumedAppointmentReservations);
            appointment.setStatus(AppointmentStatus.COMPLETED);
        }

        BigDecimal taxRate = invoice.getTaxRate() == null ? new BigDecimal("0.13") : invoice.getTaxRate();

        if (taxRate.compareTo(BigDecimal.ONE) > 0) {
            taxRate = taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        }

        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxRate(taxRate);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);
    }

    private void prepareInvoiceLifecycle(Invoice invoice) {
        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            invoice.setPaidAt(LocalDateTime.now());
            return;
        }

        if (invoice.getDueDate() == null) {
            invoice.setDueDate(LocalDate.now().plusDays(14));
        }
    }

    private void linkCustomerByPhone(Invoice invoice) {
        if (invoice.getCustomerId() == null && invoice.getPhone() != null) {
            userRepository.findByPhone(invoice.getPhone()).ifPresent(user -> invoice.setCustomerId(user.getId()));
        }
    }

    private void handleTireItem(InvoiceItem item, Appointment appointment, Map<Long, Integer> consumedAppointmentReservations) {
        if (item.getTireId() == null) {
            throw new RuntimeException("Tire item must have a tireId");
        }

        Tire tire = tireRepository.findById(item.getTireId())
                .orElseThrow(() -> new RuntimeException("Tire not found"));

        int reservedForAppointment = getReservedForAppointment(appointment, item.getTireId());
        int alreadyConsumedForAppointment = consumedAppointmentReservations.getOrDefault(item.getTireId(), 0);
        int remainingReservedForAppointment = Math.max(0, reservedForAppointment - alreadyConsumedForAppointment);
        int reservedToConsume = Math.min(item.getQuantity(), remainingReservedForAppointment);
        int availableToConsume = item.getQuantity() - reservedToConsume;

        if (item.getQuantity() > tire.getQuantity()) {
            throw new RuntimeException("Invoice quantity is greater than physical tire stock");
        }

        if (availableToConsume > 0 && tire.getAvailableQuantity() < availableToConsume) {
            throw new RuntimeException("Not enough tire stock");
        }

        tire.setQuantity(tire.getQuantity() - item.getQuantity());
        tire.setReservedQuantity(Math.max(0, tire.getReservedQuantity() - reservedToConsume));
        consumedAppointmentReservations.merge(item.getTireId(), reservedToConsume, Integer::sum);

        if (item.getItemName() == null || item.getItemName().isBlank()) {
            item.setItemName(tire.getBrand() + " " + tire.getTireSize());
        }

        if (item.getUnitPrice() == null) {
            item.setUnitPrice(tire.getPrice());
        }
    }

    private int getReservedForAppointment(Appointment appointment, Long tireId) {
        if (appointment == null || tireId == null) {
            return 0;
        }

        int quantity = 0;

        if (tireId.equals(appointment.getFrontTireId())) {
            quantity += appointment.getFrontQuantity();
        }

        if (tireId.equals(appointment.getRearTireId())) {
            quantity += appointment.getRearQuantity();
        }

        return quantity;
    }

    private void releaseRemainingAppointmentReservations(Appointment appointment, Map<Long, Integer> consumedAppointmentReservations) {
        releaseRemainingAppointmentReservation(
                appointment.getFrontTireId(),
                appointment.getFrontQuantity(),
                consumedAppointmentReservations
        );
        releaseRemainingAppointmentReservation(
                appointment.getRearTireId(),
                appointment.getRearQuantity(),
                consumedAppointmentReservations
        );
    }

    private void releaseRemainingAppointmentReservation(Long tireId, int appointmentQuantity, Map<Long, Integer> consumedAppointmentReservations) {
        if (tireId == null || appointmentQuantity <= 0) {
            return;
        }

        int consumed = consumedAppointmentReservations.getOrDefault(tireId, 0);
        int remainingToRelease = Math.max(0, appointmentQuantity - consumed);

        if (remainingToRelease <= 0) {
            return;
        }

        Tire tire = tireRepository.findById(tireId)
                .orElseThrow(() -> new RuntimeException("Tire not found"));

        tire.setReservedQuantity(Math.max(0, tire.getReservedQuantity() - remainingToRelease));
        consumedAppointmentReservations.merge(tireId, remainingToRelease, Integer::sum);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getName() != null
                ? authentication.getName()
                : "system";
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
