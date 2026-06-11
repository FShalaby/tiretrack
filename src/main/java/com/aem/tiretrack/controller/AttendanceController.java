package com.aem.tiretrack.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.EmployeeAttendanceResponse;
import com.aem.tiretrack.dto.ResolveAbsenceRequest;
import com.aem.tiretrack.dto.UserResponse;
import com.aem.tiretrack.model.EmployeeAttendance;
import com.aem.tiretrack.service.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public EmployeeAttendanceResponse clockIn(@RequestParam(required = false) Long locationId) {
        return new EmployeeAttendanceResponse(attendanceService.clockInCurrentUser(locationId));
    }

    @PostMapping("/clock-out")
    public EmployeeAttendanceResponse clockOut() {
        return new EmployeeAttendanceResponse(attendanceService.clockOutCurrentUser());
    }

    @GetMapping("/me/today")
    public EmployeeAttendanceResponse getMyTodayAttendance() {
        EmployeeAttendance attendance = attendanceService.getMyTodayAttendance();
        return attendance == null ? null : new EmployeeAttendanceResponse(attendance);
    }

    @GetMapping("/me/range")
    public List<EmployeeAttendanceResponse> getMyAttendanceRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return attendanceService.getMyAttendanceRange(start, end).stream()
                .map(EmployeeAttendanceResponse::new)
                .toList();
    }

    @GetMapping("/employees")
    public List<UserResponse> getEmployees() {
        return attendanceService.getEmployees().stream().map(UserResponse::new).toList();
    }

    @GetMapping("/day")
    public List<EmployeeAttendanceResponse> getAttendanceByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long locationId) {
        return attendanceService.getAttendanceByDate(date, locationId).stream()
                .map(EmployeeAttendanceResponse::new)
                .toList();
    }

    @GetMapping("/employee/{employeeId}/range")
    public List<EmployeeAttendanceResponse> getEmployeeAttendanceRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long locationId) {
        return attendanceService.getEmployeeAttendanceRange(employeeId, start, end, locationId).stream()
                .map(EmployeeAttendanceResponse::new)
                .toList();
    }

    @PostMapping("/employee/{employeeId}/absent")
    public EmployeeAttendanceResponse markEmployeeAbsent(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new EmployeeAttendanceResponse(attendanceService.markEmployeeAbsent(employeeId, date));
    }

    @PostMapping("/{attendanceId}/resolve-absence")
    public EmployeeAttendanceResponse resolveAbsence(
            @PathVariable Long attendanceId,
            @RequestBody ResolveAbsenceRequest request) {
        return new EmployeeAttendanceResponse(attendanceService.resolveAbsence(
                attendanceId,
                request.getDecision(),
                request.getNotes()));
    }

    @GetMapping("/absences/unresolved")
    public List<EmployeeAttendanceResponse> getUnresolvedAbsences() {
        return attendanceService.getUnresolvedAbsences().stream()
                .map(EmployeeAttendanceResponse::new)
                .toList();
    }
}
