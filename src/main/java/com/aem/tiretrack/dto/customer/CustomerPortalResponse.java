package com.aem.tiretrack.dto.customer;

import java.util.List;

import com.aem.tiretrack.dto.EstimateResponse;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Invoice;

public class CustomerPortalResponse {
    private CustomerProfile customer;
    private List<CustomerVehicle> vehicles;
    private List<Appointment> appointments;
    private List<Invoice> invoices;
    private List<EstimateResponse> estimates;
    private List<CustomerNotification> notifications;

    public CustomerPortalResponse(CustomerProfile customer, List<CustomerVehicle> vehicles, List<Appointment> appointments, List<Invoice> invoices, List<EstimateResponse> estimates, List<CustomerNotification> notifications) {
        this.customer = customer;
        this.vehicles = vehicles;
        this.appointments = appointments;
        this.invoices = invoices;
        this.estimates = estimates;
        this.notifications = notifications;
    }

    public CustomerProfile getCustomer() { return customer; }
    public List<CustomerVehicle> getVehicles() { return vehicles; }
    public List<Appointment> getAppointments() { return appointments; }
    public List<Invoice> getInvoices() { return invoices; }
    public List<EstimateResponse> getEstimates() { return estimates; }
    public List<CustomerNotification> getNotifications() { return notifications; }
}
