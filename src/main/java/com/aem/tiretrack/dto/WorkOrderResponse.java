package com.aem.tiretrack.dto;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.enums.WorkOrderStatus;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkOrder;

public class WorkOrderResponse {
    private final Long id;
    private final Long shopId;
    private final String shopName;
    private final Long locationId;
    private final String locationName;
    private final Long appointmentId;
    private final LocalDateTime appointmentDate;
    private final Long customerId;
    private final String customerName;
    private final String phone;
    private final String email;
    private final String vehicle;
    private final Long assignedEmployeeId;
    private final String assignedEmployeeName;
    private final WorkOrderStatus status;
    private final ServiceType serviceType;
    private final String notes;
    private final Long invoiceId;
    private final LocalDateTime startedAt;
    private final LocalDateTime vehicleReadyAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public WorkOrderResponse(WorkOrder workOrder) {
        Shop shop = workOrder.getShop();
        Appointment appointment = workOrder.getAppointment();
        User customer = workOrder.getCustomer();
        User assignedEmployee = workOrder.getAssignedEmployee();

        this.id = workOrder.getId();
        this.shopId = shop == null ? null : shop.getId();
        this.shopName = shop == null ? null : shop.getName();
        this.locationId = workOrder.getLocationId();
        this.locationName = workOrder.getLocationName();
        this.appointmentId = appointment == null ? null : appointment.getId();
        this.appointmentDate = appointment == null ? null : appointment.getAppointmentDate();
        this.customerId = customer == null ? null : customer.getId();
        this.customerName = workOrder.getCustomerName();
        this.phone = workOrder.getPhone();
        this.email = workOrder.getEmail();
        this.vehicle = workOrder.getVehicle();
        this.assignedEmployeeId = assignedEmployee == null ? null : assignedEmployee.getId();
        this.assignedEmployeeName = assignedEmployee == null ? null : assignedEmployee.getFullName();
        this.status = workOrder.getStatus();
        this.serviceType = workOrder.getServiceType();
        this.notes = workOrder.getNotes();
        this.invoiceId = workOrder.getInvoiceId();
        this.startedAt = workOrder.getStartedAt();
        this.vehicleReadyAt = workOrder.getVehicleReadyAt();
        this.completedAt = workOrder.getCompletedAt();
        this.createdAt = workOrder.getCreatedAt();
        this.updatedAt = workOrder.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public Long getAppointmentId() { return appointmentId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getVehicle() { return vehicle; }
    public Long getAssignedEmployeeId() { return assignedEmployeeId; }
    public String getAssignedEmployeeName() { return assignedEmployeeName; }
    public WorkOrderStatus getStatus() { return status; }
    public ServiceType getServiceType() { return serviceType; }
    public String getNotes() { return notes; }
    public Long getInvoiceId() { return invoiceId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getVehicleReadyAt() { return vehicleReadyAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
