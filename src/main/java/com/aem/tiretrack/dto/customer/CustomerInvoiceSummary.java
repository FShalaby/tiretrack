package com.aem.tiretrack.dto.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.model.Invoice;

public class CustomerInvoiceSummary {
    private Long id;
    private String status;
    private BigDecimal total;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private String vehicle;

    public CustomerInvoiceSummary(Invoice invoice) {
        this.id = invoice.getId();
        this.status = invoice.getStatus();
        this.total = invoice.getTotal();
        BigDecimal safeTotal = invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal();
        this.amountPaid = invoice.getAmountPaid() == null && "PAID".equalsIgnoreCase(invoice.getStatus())
                ? safeTotal
                : invoice.getAmountPaid();
        this.balanceDue = invoice.getBalanceDue() == null
                ? safeTotal.subtract(this.amountPaid == null ? BigDecimal.ZERO : this.amountPaid).max(BigDecimal.ZERO)
                : invoice.getBalanceDue();
        this.dueDate = invoice.getDueDate();
        this.createdAt = invoice.getCreatedAt();
        this.vehicle = invoice.getVehicle();
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public BigDecimal getBalanceDue() { return balanceDue; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getVehicle() { return vehicle; }
}
