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
import com.aem.tiretrack.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController 
{
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments().stream().map(AppointmentResponse::new).toList();
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

}
