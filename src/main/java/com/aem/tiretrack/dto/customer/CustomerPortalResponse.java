package com.aem.tiretrack.dto.customer;

import java.util.List;

import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.User;

public class CustomerPortalResponse {
    private User customer;
    private List<CustomerVehicle> vehicles;
    private List<Appointment> appointments;
    private List<Invoice> invoices;
    private List<CustomerNotification> notifications;

    public CustomerPortalResponse(User customer, List<CustomerVehicle> vehicles, List<Appointment> appointments, List<Invoice> invoices, List<CustomerNotification> notifications) {
        this.customer = customer;
        this.vehicles = vehicles;
        this.appointments = appointments;
        this.invoices = invoices;
        this.notifications = notifications;
    }

    public User getCustomer() { return customer; }
    public List<CustomerVehicle> getVehicles() { return vehicles; }
    public List<Appointment> getAppointments() { return appointments; }
    public List<Invoice> getInvoices() { return invoices; }
    public List<CustomerNotification> getNotifications() { return notifications; }
}
