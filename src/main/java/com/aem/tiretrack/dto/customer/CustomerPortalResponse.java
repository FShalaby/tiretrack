package com.aem.tiretrack.dto.customer;

import java.util.List;

import com.aem.tiretrack.dto.AppointmentResponse;
import com.aem.tiretrack.dto.EstimateResponse;
import com.aem.tiretrack.dto.InvoiceResponse;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Invoice;

public class CustomerPortalResponse {
    private CustomerProfile customer;
    private List<CustomerVehicleResponse> vehicles;
    private List<AppointmentResponse> appointments;
    private List<InvoiceResponse> invoices;
    private List<EstimateResponse> estimates;
    private List<CustomerNotificationResponse> notifications;

    public CustomerPortalResponse(CustomerProfile customer, List<CustomerVehicle> vehicles, List<Appointment> appointments, List<Invoice> invoices, List<EstimateResponse> estimates, List<CustomerNotification> notifications) {
        this.customer = customer;
        this.vehicles = vehicles.stream().map(CustomerVehicleResponse::new).toList();
        this.appointments = appointments.stream().map(AppointmentResponse::new).toList();
        this.invoices = invoices.stream().map(InvoiceResponse::new).toList();
        this.estimates = estimates;
        this.notifications = notifications.stream().map(CustomerNotificationResponse::new).toList();
    }

    public CustomerProfile getCustomer() { return customer; }
    public List<CustomerVehicleResponse> getVehicles() { return vehicles; }
    public List<AppointmentResponse> getAppointments() { return appointments; }
    public List<InvoiceResponse> getInvoices() { return invoices; }
    public List<EstimateResponse> getEstimates() { return estimates; }
    public List<CustomerNotificationResponse> getNotifications() { return notifications; }
}
