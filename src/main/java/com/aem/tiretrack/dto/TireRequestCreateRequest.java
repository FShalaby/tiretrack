package com.aem.tiretrack.dto;

import com.aem.tiretrack.enums.TireRequestSource;

public class TireRequestCreateRequest {
    private Long customerId;
    private Long vehicleId;
    private Long locationId;
    private Long appointmentId;
    private String requestedSize;
    private TireRequestSource source;
    private String notes;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getRequestedSize() { return requestedSize; }
    public void setRequestedSize(String requestedSize) { this.requestedSize = requestedSize; }
    public TireRequestSource getSource() { return source; }
    public void setSource(TireRequestSource source) { this.source = source; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
