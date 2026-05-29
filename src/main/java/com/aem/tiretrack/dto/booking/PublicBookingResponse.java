package com.aem.tiretrack.dto.booking;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.model.Appointment;

public class PublicBookingResponse {
    private String message;
    private Long appointmentId;
    private String customerName;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;

    public PublicBookingResponse(Appointment appointment) {
        this.message = "Booking request confirmed";
        this.appointmentId = appointment.getId();
        this.customerName = appointment.getCustomerName();
        this.appointmentDate = appointment.getAppointmentDate();
        this.status = appointment.getStatus();
    }

    public String getMessage() { return message; }
    public Long getAppointmentId() { return appointmentId; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public AppointmentStatus getStatus() { return status; }
}
