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
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.service.AppointmentService;
import com.aem.tiretrack.service.ShopContextService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController 
{
    private final AppointmentService appointmentService;
    private final ShopContextService shopContextService;

    public AppointmentController(AppointmentService appointmentService, ShopContextService shopContextService) {
        this.appointmentService = appointmentService;
        this.shopContextService = shopContextService;
    }

    @GetMapping
    public List<AppointmentResponse> getAllAppointments(@org.springframework.web.bind.annotation.RequestParam(required = false) Long locationId) {
        ShopLocation location = shopContextService.resolveAccessibleLocation(locationId, null, false).orElse(null);
        return appointmentService.getAllAppointments().stream()
                .filter(appointment -> matchesLocation(appointment.getShopLocation(), location))
                .map(AppointmentResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(@PathVariable Long id) {
        return new AppointmentResponse(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    public AppointmentResponse createAppointment(@Valid @RequestBody Appointment appointment) {
        return new AppointmentResponse(appointmentService.saveAppointment(appointment));
    }

    @PutMapping("/{id}")
    public AppointmentResponse updateAppointment(
            @PathVariable Long id,
           @Valid @RequestBody Appointment updatedAppointment) {

        return new AppointmentResponse(appointmentService.updateAppointment(id, updatedAppointment));
    }

    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }

    private boolean matchesLocation(ShopLocation resourceLocation, ShopLocation requestedLocation) {
        if (requestedLocation == null) {
            return true;
        }

        return resourceLocation != null && requestedLocation.getId().equals(resourceLocation.getId());
    }

}
