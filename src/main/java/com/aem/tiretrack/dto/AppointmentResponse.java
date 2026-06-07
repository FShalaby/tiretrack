package com.aem.tiretrack.dto;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.model.Appointment;

public class AppointmentResponse {
    private Long id;
    private String customerName;
    private String phone;
    private String email;
    private String vehicle;
    private Long customerId;
    private Long shopId;
    private String shopName;
    private Long locationId;
    private String locationName;
    private String tireSize;
    private Long frontTireId;
    private int frontQuantity;
    private Long rearTireId;
    private int rearQuantity;
    private LocalDateTime appointmentDate;
    private ServiceType serviceType;
    private String notes;
    private String reminderStatus;
    private LocalDateTime reminderAt;
    private String confirmationStatus;
    private String cancelReason;
    private AppointmentStatus status;
    private LocalDateTime createdAt;

    public AppointmentResponse(Appointment appointment) {
        this.id = appointment.getId();
        this.customerName = appointment.getCustomerName();
        this.phone = appointment.getPhone();
        this.email = appointment.getEmail();
        this.vehicle = appointment.getVehicle();
        this.customerId = appointment.getCustomerId();
        this.shopId = appointment.getShopId();
        this.shopName = appointment.getShopName();
        this.locationId = appointment.getLocationId();
        this.locationName = appointment.getLocationName();
        this.tireSize = appointment.getTireSize();
        this.frontTireId = appointment.getFrontTireId();
        this.frontQuantity = appointment.getFrontQuantity();
        this.rearTireId = appointment.getRearTireId();
        this.rearQuantity = appointment.getRearQuantity();
        this.appointmentDate = appointment.getAppointmentDate();
        this.serviceType = appointment.getServiceType();
        this.notes = appointment.getNotes();
        this.reminderStatus = appointment.getReminderStatus();
        this.reminderAt = appointment.getReminderAt();
        this.confirmationStatus = appointment.getConfirmationStatus();
        this.cancelReason = appointment.getCancelReason();
        this.status = appointment.getStatus();
        this.createdAt = appointment.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getVehicle() { return vehicle; }
    public Long getCustomerId() { return customerId; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getTireSize() { return tireSize; }
    public Long getFrontTireId() { return frontTireId; }
    public int getFrontQuantity() { return frontQuantity; }
    public Long getRearTireId() { return rearTireId; }
    public int getRearQuantity() { return rearQuantity; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public ServiceType getServiceType() { return serviceType; }
    public String getNotes() { return notes; }
    public String getReminderStatus() { return reminderStatus; }
    public LocalDateTime getReminderAt() { return reminderAt; }
    public String getConfirmationStatus() { return confirmationStatus; }
    public String getCancelReason() { return cancelReason; }
    public AppointmentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
