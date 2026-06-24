package com.aem.tiretrack.dto;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.TireRequestSource;
import com.aem.tiretrack.enums.TireRequestStatus;
import com.aem.tiretrack.model.TireRequest;

public class TireRequestResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vehicle;
    private Long shopId;
    private String shopName;
    private Long locationId;
    private String locationName;
    private Long appointmentId;
    private LocalDateTime appointmentDate;
    private String requestedSize;
    private TireRequestStatus status;
    private Long requestedBy;
    private TireRequestSource source;
    private String notes;
    private String adminResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TireRequestResponse(TireRequest request) {
        this.id = request.getId();
        this.customerId = request.getCustomerId();
        this.customerName = request.getCustomerName();
        this.vehicleId = request.getVehicleId();
        this.vehicle = request.getVehicleLabel();
        this.shopId = request.getShopId();
        this.shopName = request.getShopName();
        this.locationId = request.getLocationId();
        this.locationName = request.getLocationName();
        this.appointmentId = request.getAppointmentId();
        this.appointmentDate = request.getAppointmentDate();
        this.requestedSize = request.getRequestedSize();
        this.status = request.getStatus();
        this.requestedBy = request.getRequestedBy();
        this.source = request.getSource();
        this.notes = request.getNotes();
        this.adminResponse = request.getAdminResponse();
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Long getVehicleId() { return vehicleId; }
    public String getVehicle() { return vehicle; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public Long getAppointmentId() { return appointmentId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public String getRequestedSize() { return requestedSize; }
    public TireRequestStatus getStatus() { return status; }
    public Long getRequestedBy() { return requestedBy; }
    public TireRequestSource getSource() { return source; }
    public String getNotes() { return notes; }
    public String getAdminResponse() { return adminResponse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
