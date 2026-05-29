package com.aem.tiretrack.dto.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.model.Invoice;

public class CustomerInvoiceSummary {
    private Long id;
    private String status;
    private BigDecimal total;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private String vehicle;

    public CustomerInvoiceSummary(Invoice invoice) {
        this.id = invoice.getId();
        this.status = invoice.getStatus();
        this.total = invoice.getTotal();
        this.dueDate = invoice.getDueDate();
        this.createdAt = invoice.getCreatedAt();
        this.vehicle = invoice.getVehicle();
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getVehicle() { return vehicle; }
}
