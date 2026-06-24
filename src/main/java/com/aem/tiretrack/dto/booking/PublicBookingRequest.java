package com.aem.tiretrack.dto.booking;

import java.time.LocalDate;
import java.time.LocalTime;

import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.util.PhoneNumberUtils;
import com.aem.tiretrack.util.TireSizeUtils;

public class PublicBookingRequest {
    private String customerName;
    private String email;
    private String phone;
    private String vehicle;
    private String tireSize;
    private Long shopId;
    private Long locationId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private ServiceType serviceType;
    private String notes;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = PhoneNumberUtils.formatCanadian(phone);
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getTireSize() {
        return tireSize;
    }

    public void setTireSize(String tireSize) {
        this.tireSize = TireSizeUtils.formatPassengerSize(tireSize);
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
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
}
