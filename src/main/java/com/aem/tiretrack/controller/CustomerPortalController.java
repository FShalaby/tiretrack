package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.customer.CustomerAppointmentRequest;
import com.aem.tiretrack.dto.EstimateResponse;
import com.aem.tiretrack.dto.customer.CustomerNoticeRequest;
import com.aem.tiretrack.dto.customer.CustomerPortalResponse;
import com.aem.tiretrack.dto.customer.CustomerSummary;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CustomerNotification;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.service.CustomerPortalService;

@RestController
public class CustomerPortalController {
    private final CustomerPortalService customerPortalService;

    public CustomerPortalController(CustomerPortalService customerPortalService) {
        this.customerPortalService = customerPortalService;
    }

    @GetMapping("/api/customer/portal")
    public CustomerPortalResponse portal() {
        return customerPortalService.portal();
    }

    @PostMapping("/api/customer/vehicles")
    public CustomerVehicle saveVehicle(@RequestBody CustomerVehicle vehicle) {
        return customerPortalService.saveVehicle(vehicle);
    }

    @DeleteMapping("/api/customer/vehicles/{id}")
    public void deleteVehicle(@PathVariable Long id) {
        customerPortalService.deleteVehicle(id);
    }

    @PostMapping("/api/customer/appointments")
    public Appointment bookAppointment(@RequestBody CustomerAppointmentRequest request) {
        return customerPortalService.bookAppointment(request);
    }

    @PostMapping("/api/customer/invoices/{id}/pay")
    public Invoice payInvoice(@PathVariable Long id) {
        return customerPortalService.payInvoice(id);
    }

    @PostMapping("/api/customer/estimates/{id}/approve")
    public EstimateResponse approveEstimate(@PathVariable Long id) {
        return customerPortalService.approveEstimate(id);
    }

    @PutMapping("/api/customer/notifications/{id}/read")
    public CustomerNotification markNotificationRead(@PathVariable Long id) {
        return customerPortalService.markNotificationRead(id);
    }

    @GetMapping("/api/customers")
    public List<CustomerSummary> customers() {
        return customerPortalService.adminSummaries();
    }

    @PostMapping("/api/customers/{id}/notices")
    public CustomerNotification sendNotice(@PathVariable Long id, @RequestBody CustomerNoticeRequest request) {
        return customerPortalService.sendNotice(id, request);
    }
}
