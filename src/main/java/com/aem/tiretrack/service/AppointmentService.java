package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.TireRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TireRepository tireRepository;
    private final AuditLogService auditLogService;

    public AppointmentService(AppointmentRepository appointmentRepository, TireRepository tireRepository, AuditLogService auditLogService) {
        this.appointmentRepository = appointmentRepository;
        this.tireRepository = tireRepository;
        this.auditLogService = auditLogService;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    @Transactional
    public Appointment saveAppointment(Appointment appointment) {
        validateAppointmentTime(appointment, null);
        reserveAppointmentTires(appointment);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        auditLogService.record("BOOKED", "Appointment", savedAppointment.getId(), "Booked appointment for " + savedAppointment.getCustomerName());
        return savedAppointment;
    }

    @Transactional
    public Appointment updateAppointment(Long id, Appointment updatedAppointment) {

        Appointment existingAppointment = getAppointmentById(id);
        validateAppointmentTime(updatedAppointment, id);
        releaseAppointmentTires(existingAppointment);

        existingAppointment.setCustomerName(updatedAppointment.getCustomerName());
        existingAppointment.setPhone(updatedAppointment.getPhone());
        existingAppointment.setVehicle(updatedAppointment.getVehicle());
        existingAppointment.setTireSize(updatedAppointment.getTireSize());
        existingAppointment.setFrontTireId(updatedAppointment.getFrontTireId());
        existingAppointment.setFrontQuantity(updatedAppointment.getFrontQuantity());
        existingAppointment.setRearTireId(updatedAppointment.getRearTireId());
        existingAppointment.setRearQuantity(updatedAppointment.getRearQuantity());
        existingAppointment.setAppointmentDate(updatedAppointment.getAppointmentDate());
        existingAppointment.setServiceType(updatedAppointment.getServiceType());
        existingAppointment.setNotes(updatedAppointment.getNotes());
        existingAppointment.setReminderStatus(updatedAppointment.getReminderStatus());
        existingAppointment.setReminderAt(updatedAppointment.getReminderAt());
        existingAppointment.setConfirmationStatus(updatedAppointment.getConfirmationStatus());
        existingAppointment.setCancelReason(updatedAppointment.getCancelReason());

        if (updatedAppointment.getStatus() != null) {
            existingAppointment.setStatus(updatedAppointment.getStatus());
        }

        reserveAppointmentTires(existingAppointment);
        Appointment savedAppointment = appointmentRepository.save(existingAppointment);
        auditLogService.record("UPDATED", "Appointment", savedAppointment.getId(), "Updated appointment for " + savedAppointment.getCustomerName());
        return savedAppointment;
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        releaseAppointmentTires(appointment);
        appointmentRepository.delete(appointment);
        auditLogService.record("DELETED", "Appointment", id, "Deleted appointment for " + appointment.getCustomerName());
    }

    private void reserveAppointmentTires(Appointment appointment) {
        if (!shouldReserve(appointment)) {
            return;
        }

        changeReservation(appointment.getFrontTireId(), appointment.getFrontQuantity(), 1);
        changeReservation(appointment.getRearTireId(), appointment.getRearQuantity(), 1);
    }

    private void releaseAppointmentTires(Appointment appointment) {
        if (!shouldReserve(appointment)) {
            return;
        }

        changeReservation(appointment.getFrontTireId(), appointment.getFrontQuantity(), -1);
        changeReservation(appointment.getRearTireId(), appointment.getRearQuantity(), -1);
    }

    private boolean shouldReserve(Appointment appointment) {
        return appointment.getStatus() == null || appointment.getStatus() == AppointmentStatus.BOOKED;
    }

    private void validateAppointmentTime(Appointment appointment, Long ignoredAppointmentId) {
        if (appointment.getAppointmentDate() == null || !shouldReserve(appointment)) {
            return;
        }

        boolean hasConflict = appointmentRepository.findByAppointmentDate(appointment.getAppointmentDate()).stream()
                .filter(existingAppointment -> ignoredAppointmentId == null || !ignoredAppointmentId.equals(existingAppointment.getId()))
                .anyMatch(this::shouldReserve);

        if (hasConflict) {
            throw new RuntimeException("That appointment time is already booked");
        }
    }

    private void changeReservation(Long tireId, int quantity, int direction) {
        if (tireId == null || quantity <= 0) {
            return;
        }

        Tire tire = tireRepository.findById(tireId)
                .orElseThrow(() -> new RuntimeException("Tire not found"));

        if (direction > 0 && tire.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Not enough available tire stock to reserve");
        }

        int nextReservedQuantity = tire.getReservedQuantity() + (quantity * direction);

        if (nextReservedQuantity < 0) {
            nextReservedQuantity = 0;
        }

        tire.setReservedQuantity(nextReservedQuantity);
    }
}
