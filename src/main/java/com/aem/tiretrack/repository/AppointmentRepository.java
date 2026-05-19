package com.aem.tiretrack.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aem.tiretrack.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> 
{
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate BETWEEN :start AND :end AND a.status = com.aem.tiretrack.enums.AppointmentStatus.BOOKED")
    long countTodayAppointments(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Appointment> findByAppointmentDate(LocalDateTime appointmentDate);

    List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByCustomerIdOrderByAppointmentDateDesc(Long customerId);

    @Query("select a from Appointment a where a.customerId = :customerId or a.phone = :phone order by a.appointmentDate desc")
    List<Appointment> findCustomerHistory(@Param("customerId") Long customerId, @Param("phone") String phone);

}
