package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.enums.InvoiceItemType;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.InvoiceItem;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.TireRepository;

@Service
public class InvoiceService {
    private static final BigDecimal ONTARIO_HST_RATE = new BigDecimal("0.13");

    private final InvoiceRepository invoiceRepository;
    private final TireRepository tireRepository;
    private final AppointmentRepository appointmentRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, TireRepository tireRepository, AppointmentRepository appointmentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.tireRepository = tireRepository;
        this.appointmentRepository = appointmentRepository;
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
            return invoiceRepository.saveAndFlush(invoice);
        } catch (RuntimeException exception) {
            throw new RuntimeException(rootMessage(exception), exception);
        }
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
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

        BigDecimal taxAmount = subtotal.multiply(ONTARIO_HST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxRate(ONTARIO_HST_RATE);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);
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

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
