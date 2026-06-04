package com.aem.tiretrack.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.enums.AbsenceDecision;
import com.aem.tiretrack.enums.AttendanceStatus;
import com.aem.tiretrack.model.EmployeeAttendance;

public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Long> {
    @Override
    @EntityGraph(attributePaths = { "employee", "shop" })
    Optional<EmployeeAttendance> findById(Long id);

    @EntityGraph(attributePaths = { "employee", "shop" })
    Optional<EmployeeAttendance> findByEmployee_IdAndWorkDate(Long employeeId, LocalDate workDate);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByEmployee_IdAndWorkDateBetween(Long employeeId, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByWorkDateBetween(LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByShop_IdAndWorkDateBetween(Long shopId, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByWorkDate(LocalDate workDate);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByShop_IdAndWorkDate(Long shopId, LocalDate workDate);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByStatus(AttendanceStatus status);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByAbsenceDecision(AbsenceDecision absenceDecision);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByStatusAndAbsenceDecision(AttendanceStatus status, AbsenceDecision decision);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeAttendance> findByShop_IdAndStatusAndAbsenceDecision(Long shopId, AttendanceStatus status, AbsenceDecision decision);
}
