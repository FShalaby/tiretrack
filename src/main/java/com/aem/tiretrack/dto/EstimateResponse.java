package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.aem.tiretrack.enums.EstimateStatus;
import com.aem.tiretrack.model.Estimate;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.User;

public class EstimateResponse {
    private final Long id;
    private final Long shopId;
    private final String shopName;
    private final Long locationId;
    private final String locationName;
    private final Long customerId;
    private final String customerName;
    private final String phone;
    private final String email;
    private final String vehicle;
    private final String estimateNumber;
    private final EstimateStatus status;
    private final BigDecimal subtotal;
    private final BigDecimal taxRate;
    private final BigDecimal taxAmount;
    private final BigDecimal total;
    private final String notes;
    private final LocalDate validUntil;
    private final Long convertedInvoiceId;
    private final Long appointmentId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<EstimateItemResponse> items;

    public EstimateResponse(Estimate estimate) {
        Shop shop = estimate.getShop();
        User customer = estimate.getCustomer();

        this.id = estimate.getId();
        this.shopId = shop == null ? null : shop.getId();
        this.shopName = shop == null ? null : shop.getName();
        this.locationId = estimate.getLocationId();
        this.locationName = estimate.getLocationName();
        this.customerId = customer == null ? null : customer.getId();
        this.customerName = estimate.getCustomerName();
        this.phone = estimate.getPhone();
        this.email = estimate.getEmail();
        this.vehicle = estimate.getVehicle();
        this.estimateNumber = estimate.getEstimateNumber();
        this.status = estimate.getStatus();
        this.subtotal = estimate.getSubtotal();
        this.taxRate = estimate.getTaxRate();
        this.taxAmount = estimate.getTaxAmount();
        this.total = estimate.getTotal();
        this.notes = estimate.getNotes();
        this.validUntil = estimate.getValidUntil();
        this.convertedInvoiceId = estimate.getConvertedInvoiceId();
        this.appointmentId = estimate.getAppointmentId();
        this.createdAt = estimate.getCreatedAt();
        this.updatedAt = estimate.getUpdatedAt();
        this.items = estimate.getItems().stream().map(EstimateItemResponse::new).toList();
    }

    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getVehicle() { return vehicle; }
    public String getEstimateNumber() { return estimateNumber; }
    public EstimateStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotal() { return total; }
    public String getNotes() { return notes; }
    public LocalDate getValidUntil() { return validUntil; }
    public Long getConvertedInvoiceId() { return convertedInvoiceId; }
    public Long getAppointmentId() { return appointmentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<EstimateItemResponse> getItems() { return items; }
}
