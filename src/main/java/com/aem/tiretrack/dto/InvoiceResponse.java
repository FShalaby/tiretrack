package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.aem.tiretrack.model.Invoice;

public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private String customerName;
    private String phone;
    private String vehicle;
    private Long customerId;
    private Long shopId;
    private String shopName;
    private Long locationId;
    private String locationName;
    private Long appointmentId;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String paymentMethod;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private List<InvoiceItemResponse> items;

    public InvoiceResponse(Invoice invoice) {
        this.id = invoice.getId();
        this.invoiceNumber = formatInvoiceNumber(invoice.getId());
        this.customerName = invoice.getCustomerName();
        this.phone = invoice.getPhone();
        this.vehicle = invoice.getVehicle();
        this.customerId = invoice.getCustomerId();
        this.shopId = invoice.getShopId();
        this.shopName = invoice.getShopName();
        this.locationId = invoice.getLocationId();
        this.locationName = invoice.getLocationName();
        this.appointmentId = invoice.getAppointmentId();
        this.subtotal = invoice.getSubtotal();
        this.taxRate = invoice.getTaxRate();
        this.taxAmount = invoice.getTaxAmount();
        this.total = invoice.getTotal();
        this.amountPaid = invoice.getAmountPaid();
        this.balanceDue = invoice.getBalanceDue();
        this.paymentMethod = invoice.getPaymentMethod();
        this.status = invoice.getStatus();
        this.dueDate = invoice.getDueDate();
        this.paidAt = invoice.getPaidAt();
        this.createdAt = invoice.getCreatedAt();
        this.items = invoice.getItems().stream().map(InvoiceItemResponse::new).toList();
    }

    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getVehicle() { return vehicle; }
    public Long getCustomerId() { return customerId; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public Long getAppointmentId() { return appointmentId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public BigDecimal getBalanceDue() { return balanceDue; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<InvoiceItemResponse> getItems() { return items; }

    private String formatInvoiceNumber(Long id) {
        return id == null ? null : "INV-" + String.format("%06d", id);
    }
}
