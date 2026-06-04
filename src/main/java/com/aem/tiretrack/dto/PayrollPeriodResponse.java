package com.aem.tiretrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.PayrollStatus;
import com.aem.tiretrack.model.PayrollPeriod;

public class PayrollPeriodResponse {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private PayrollStatus status;
    private LocalDateTime paidAt;
    private String notes;
    private Long shopId;
    private String shopName;
    private Long locationId;
    private String locationName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PayrollPeriodResponse(PayrollPeriod period) {
        this.id = period.getId();
        this.startDate = period.getStartDate();
        this.endDate = period.getEndDate();
        this.status = period.getStatus();
        this.paidAt = period.getPaidAt();
        this.notes = period.getNotes();
        this.shopId = period.getShopId();
        this.shopName = period.getShopName();
        this.locationId = period.getLocationId();
        this.locationName = period.getLocationName();
        this.createdAt = period.getCreatedAt();
        this.updatedAt = period.getUpdatedAt();
    }

    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public PayrollStatus getStatus() { return status; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getNotes() { return notes; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
