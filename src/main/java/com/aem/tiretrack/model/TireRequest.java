package com.aem.tiretrack.model;

import java.time.LocalDateTime;
import java.util.List;

import com.aem.tiretrack.enums.TireRequestSource;
import com.aem.tiretrack.enums.TireRequestStatus;
import com.aem.tiretrack.util.TireSizeUtils;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "tire_requests")
public class TireRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private CustomerVehicle vehicle;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ShopLocation location;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "requested_size")
    private String requestedSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TireRequestStatus status = TireRequestStatus.PENDING;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TireRequestSource source = TireRequestSource.ADMIN;

    @Column(length = 1000)
    private String notes;

    @Column(name = "admin_response", length = 1000)
    private String adminResponse;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = TireRequestStatus.PENDING;
        }
        if (source == null) {
            source = TireRequestSource.ADMIN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public CustomerVehicle getVehicle() { return vehicle; }
    public void setVehicle(CustomerVehicle vehicle) { this.vehicle = vehicle; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    public ShopLocation getLocation() { return location; }
    public void setLocation(ShopLocation location) { this.location = location; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public String getRequestedSize() { return requestedSize; }
    public void setRequestedSize(String requestedSize) { this.requestedSize = TireSizeUtils.formatPassengerSize(requestedSize); }
    public TireRequestStatus getStatus() { return status; }
    public void setStatus(TireRequestStatus status) { this.status = status; }
    public Long getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Long requestedBy) { this.requestedBy = requestedBy; }
    public TireRequestSource getSource() { return source; }
    public void setSource(TireRequestSource source) { this.source = source; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Long getCustomerId() { return customer == null ? null : customer.getId(); }
    public String getCustomerName() { return customer == null ? null : customer.getFullName(); }
    public Long getVehicleId() { return vehicle == null ? null : vehicle.getId(); }
    public String getVehicleLabel() {
        if (vehicle == null) {
            return appointment == null ? null : appointment.getVehicle();
        }

        return String.join(" ", List.of(
                vehicle.getYear() == null ? "" : vehicle.getYear(),
                vehicle.getMake() == null ? "" : vehicle.getMake(),
                vehicle.getModel() == null ? "" : vehicle.getModel()
        )).trim();
    }
    public Long getShopId() { return shop == null ? null : shop.getId(); }
    public String getShopName() { return shop == null ? null : shop.getName(); }
    public Long getLocationId() { return location == null ? null : location.getId(); }
    public String getLocationName() { return location == null ? null : location.getName(); }
    public Long getAppointmentId() { return appointment == null ? null : appointment.getId(); }
    public LocalDateTime getAppointmentDate() { return appointment == null ? null : appointment.getAppointmentDate(); }
}
