package com.aem.tiretrack.dto;

import java.util.List;

import com.aem.tiretrack.enums.TireAvailabilityStatus;

public class TireAvailabilityResponse {
    private Long vehicleId;
    private String requiredSize;
    private TireAvailabilityStatus status;
    private int selectedLocationAvailableQuantity;
    private List<TireLocationAvailabilityResponse> otherLocationAvailability;
    private List<TireLocationAvailabilityResponse> warehouseAvailability;
    private boolean tireServiceRequired;
    private boolean canConfirmAppointment;
    private String reason;

    public TireAvailabilityResponse(
            Long vehicleId,
            String requiredSize,
            TireAvailabilityStatus status,
            int selectedLocationAvailableQuantity,
            List<TireLocationAvailabilityResponse> otherLocationAvailability,
            List<TireLocationAvailabilityResponse> warehouseAvailability,
            boolean tireServiceRequired,
            boolean canConfirmAppointment,
            String reason) {
        this.vehicleId = vehicleId;
        this.requiredSize = requiredSize;
        this.status = status;
        this.selectedLocationAvailableQuantity = selectedLocationAvailableQuantity;
        this.otherLocationAvailability = otherLocationAvailability;
        this.warehouseAvailability = warehouseAvailability;
        this.tireServiceRequired = tireServiceRequired;
        this.canConfirmAppointment = canConfirmAppointment;
        this.reason = reason;
    }

    public Long getVehicleId() { return vehicleId; }
    public String getRequiredSize() { return requiredSize; }
    public TireAvailabilityStatus getStatus() { return status; }
    public int getSelectedLocationAvailableQuantity() { return selectedLocationAvailableQuantity; }
    public List<TireLocationAvailabilityResponse> getOtherLocationAvailability() { return otherLocationAvailability; }
    public List<TireLocationAvailabilityResponse> getWarehouseAvailability() { return warehouseAvailability; }
    public boolean isTireServiceRequired() { return tireServiceRequired; }
    public boolean isCanConfirmAppointment() { return canConfirmAppointment; }
    public String getReason() { return reason; }
}
