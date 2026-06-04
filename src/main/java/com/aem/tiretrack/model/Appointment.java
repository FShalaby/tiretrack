package com.aem.tiretrack.model;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name")
    private String customerName;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String email;

    private String vehicle;

    @Column(name = "customer_id")
    private Long customerId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ShopLocation shopLocation;

    @Column(name = "tire_size")
    private String tireSize;

    @Column(name = "front_tire_id")
    private Long frontTireId;

    @Column(name = "front_quantity")
    @PositiveOrZero(message = "Front quantity cannot be negative")
    private Integer frontQuantity = 0;

    @Column(name = "rear_tire_id")
    private Long rearTireId;

    @Column(name = "rear_quantity")
    @PositiveOrZero(message = "Rear quantity cannot be negative")
    private Integer rearQuantity = 0;

    @NotNull(message = "Appointment date is required")
    @Column(name = "appointment_date")
    private LocalDateTime appointmentDate;

    @NotNull(message = "Service type is required")
    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private String notes;

    @Column(name = "reminder_status")
    private String reminderStatus = "NOT_SET";

    @Column(name = "reminder_at")
    private LocalDateTime reminderAt;

    @Column(name = "confirmation_status")
    private String confirmationStatus = "PENDING";

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (status == null) {
            status = AppointmentStatus.BOOKED;
        }

        if (reminderStatus == null) {
            reminderStatus = "NOT_SET";
        }

        if (confirmationStatus == null) {
            confirmationStatus = "PENDING";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public ShopLocation getShopLocation() {
        return shopLocation;
    }

    public void setShopLocation(ShopLocation shopLocation) {
        this.shopLocation = shopLocation;
    }

    public Long getShopId() {
        return shop == null ? null : shop.getId();
    }

    public String getShopName() {
        return shop == null ? null : shop.getName();
    }

    public Long getLocationId() {
        return shopLocation == null ? null : shopLocation.getId();
    }

    public String getLocationName() {
        return shopLocation == null ? null : shopLocation.getName();
    }

    public String getTireSize() {
        return tireSize;
    }

    public void setTireSize(String tireSize) {
        this.tireSize = tireSize;
    }

    public Long getFrontTireId() {
        return frontTireId;
    }

    public void setFrontTireId(Long frontTireId) {
        this.frontTireId = frontTireId;
    }

    public int getFrontQuantity() {
        return frontQuantity == null ? 0 : frontQuantity;
    }

    public void setFrontQuantity(int frontQuantity) {
        this.frontQuantity = frontQuantity;
    }

    public Long getRearTireId() {
        return rearTireId;
    }

    public void setRearTireId(Long rearTireId) {
        this.rearTireId = rearTireId;
    }

    public int getRearQuantity() {
        return rearQuantity == null ? 0 : rearQuantity;
    }

    public void setRearQuantity(int rearQuantity) {
        this.rearQuantity = rearQuantity;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReminderStatus() {
        return reminderStatus;
    }

    public void setReminderStatus(String reminderStatus) {
        this.reminderStatus = reminderStatus;
    }

    public LocalDateTime getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(LocalDateTime reminderAt) {
        this.reminderAt = reminderAt;
    }

    public String getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(String confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
