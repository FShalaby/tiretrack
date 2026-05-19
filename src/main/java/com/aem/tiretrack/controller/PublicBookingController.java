package com.aem.tiretrack.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.booking.PublicBookingRequest;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicBookingController {

    private final AppointmentService appointmentService;

    public PublicBookingController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/bookings")
    public Appointment createPublicBooking(@Valid @RequestBody PublicBookingRequest request) {
        Appointment appointment = new Appointment();
        appointment.setCustomerName(request.getCustomerName());
        appointment.setPhone(request.getPhone());
        appointment.setVehicle(request.getVehicle());
        appointment.setTireSize(request.getTireSize());
        appointment.setAppointmentDate(request.getAppointmentDate().atTime(request.getAppointmentTime()));
        appointment.setServiceType(request.getServiceType());
        appointment.setNotes(request.getNotes());

        return appointmentService.saveAppointment(appointment);
    }

    @GetMapping("/available-slots")
    public List<String> getAvailableSlots(@RequestParam LocalDate date) {
        return appointmentService.getAvailableSlots(date);
    }
}
