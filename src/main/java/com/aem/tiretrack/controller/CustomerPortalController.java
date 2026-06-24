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

import com.aem.tiretrack.dto.AppointmentResponse;
import com.aem.tiretrack.dto.TireAvailabilityResponse;
import com.aem.tiretrack.dto.TireRequestResponse;
import com.aem.tiretrack.dto.customer.CustomerAppointmentRequest;
import com.aem.tiretrack.dto.EstimateResponse;
import com.aem.tiretrack.dto.InvoiceResponse;
import com.aem.tiretrack.dto.customer.CustomerNotificationResponse;
import com.aem.tiretrack.dto.customer.CustomerNoticeRequest;
import com.aem.tiretrack.dto.customer.CustomerPortalResponse;
import com.aem.tiretrack.dto.customer.CustomerSummary;
import com.aem.tiretrack.dto.customer.CustomerVehicleResponse;
import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.model.CustomerVehicle;
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
    public CustomerVehicleResponse saveVehicle(@RequestBody CustomerVehicle vehicle) {
        return new CustomerVehicleResponse(customerPortalService.saveVehicle(vehicle));
    }

    @DeleteMapping("/api/customer/vehicles/{id}")
    public void deleteVehicle(@PathVariable Long id) {
        customerPortalService.deleteVehicle(id);
    }

    @PostMapping("/api/customer/appointments")
    public AppointmentResponse bookAppointment(@RequestBody CustomerAppointmentRequest request) {
        return new AppointmentResponse(customerPortalService.bookAppointment(request));
    }

    @GetMapping("/api/customer/tire-availability")
    public TireAvailabilityResponse tireAvailability(
            @org.springframework.web.bind.annotation.RequestParam Long vehicleId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long locationId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "INSTALLATION") ServiceType serviceType) {
        return customerPortalService.checkTireAvailability(vehicleId, locationId, serviceType);
    }

    @GetMapping("/api/customer/tire-requests")
    public List<TireRequestResponse> tireRequests() {
        return customerPortalService.portal().getTireRequests();
    }

    @PostMapping("/api/customer/invoices/{id}/pay")
    public InvoiceResponse payInvoice(@PathVariable Long id) {
        return new InvoiceResponse(customerPortalService.payInvoice(id));
    }

    @PostMapping("/api/customer/estimates/{id}/approve")
    public EstimateResponse approveEstimate(@PathVariable Long id) {
        return customerPortalService.approveEstimate(id);
    }

    @PutMapping("/api/customer/notifications/{id}/read")
    public CustomerNotificationResponse markNotificationRead(@PathVariable Long id) {
        return new CustomerNotificationResponse(customerPortalService.markNotificationRead(id));
    }

    @GetMapping("/api/customers")
    public List<CustomerSummary> customers() {
        return customerPortalService.adminSummaries();
    }

    @PostMapping("/api/customers/{id}/notices")
    public CustomerNotificationResponse sendNotice(@PathVariable Long id, @RequestBody CustomerNoticeRequest request) {
        return new CustomerNotificationResponse(customerPortalService.sendNotice(id, request));
    }
}
