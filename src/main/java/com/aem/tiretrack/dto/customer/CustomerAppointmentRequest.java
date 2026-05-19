package com.aem.tiretrack.dto.customer;

import java.time.LocalDate;
import java.time.LocalTime;

import com.aem.tiretrack.enums.ServiceType;

public class CustomerAppointmentRequest {
    private Long vehicleId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private ServiceType serviceType;
    private String notes;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
