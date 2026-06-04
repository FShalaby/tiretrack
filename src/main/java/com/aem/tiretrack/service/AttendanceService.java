package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.enums.AbsenceDecision;
import com.aem.tiretrack.enums.AttendanceStatus;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.EmployeeAttendance;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.EmployeeAttendanceRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class AttendanceService {
    private final EmployeeAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ShopContextService shopContextService;

    public AttendanceService(
            EmployeeAttendanceRepository attendanceRepository,
            UserRepository userRepository,
            ShopContextService shopContextService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.shopContextService = shopContextService;
    }

    @Transactional
    public EmployeeAttendance clockInCurrentUser() {
        User employee = currentEmployee();
        LocalDate today = LocalDate.now();

        attendanceRepository.findByEmployee_IdAndWorkDate(employee.getId(), today).ifPresent(existing -> {
            if (existing.getClockIn() != null) {
                throw new IllegalArgumentException("You are already clocked in for today.");
            }

            throw new IllegalArgumentException("Attendance already exists for today.");
        });

        EmployeeAttendance attendance = new EmployeeAttendance();
        attendance.setEmployee(employee);
        attendance.setShop(employee.getShop());
        attendance.setShopLocation(employee.getShopLocation());
        attendance.setWorkDate(today);
        attendance.setClockIn(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setAbsenceDecision(AbsenceDecision.UNRESOLVED);
        attendance.setWorkedHours(BigDecimal.ZERO);

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public EmployeeAttendance clockOutCurrentUser() {
        User employee = currentEmployee();
        EmployeeAttendance attendance = attendanceRepository.findByEmployee_IdAndWorkDate(employee.getId(), LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("Clock in before clocking out."));

        if (attendance.getClockIn() == null) {
            throw new IllegalArgumentException("Clock in before clocking out.");
        }

        if (attendance.getClockOut() != null) {
            throw new IllegalArgumentException("You are already clocked out for today.");
        }

        LocalDateTime clockOut = LocalDateTime.now();

        if (clockOut.isBefore(attendance.getClockIn())) {
            throw new IllegalArgumentException("Clock out cannot be before clock in.");
        }

        attendance.setClockOut(clockOut);
        attendance.setWorkedHours(calculateWorkedHours(attendance.getClockIn(), clockOut));

        // TODO Payroll Phase 2: optionally sync EmployeeAttendance into WorkShift records
        // or generate payroll directly from attendance and absence decisions.
        return attendanceRepository.save(attendance);
    }

    public EmployeeAttendance getMyTodayAttendance() {
        User employee = currentEmployee();
        return attendanceRepository.findByEmployee_IdAndWorkDate(employee.getId(), LocalDate.now()).orElse(null);
    }

    public List<EmployeeAttendance> getMyAttendanceRange(LocalDate start, LocalDate end) {
        User employee = currentEmployee();
        return getEmployeeAttendanceRange(employee.getId(), start, end);
    }

    public List<EmployeeAttendance> getEmployeeAttendanceRange(Long employeeId, LocalDate start, LocalDate end) {
        validateRange(start, end);
        User employee = employeeById(employeeId);
        ensureEmployeeAccess(employee);

        return attendanceRepository.findByEmployee_IdAndWorkDateBetween(employee.getId(), start, end).stream()
                .sorted(Comparator.comparing(EmployeeAttendance::getWorkDate).reversed())
                .toList();
    }

    public List<EmployeeAttendance> getAttendanceByDate(LocalDate date) {
        LocalDate resolvedDate = date == null ? LocalDate.now() : date;

        List<EmployeeAttendance> records = attendanceRepository.findByWorkDate(resolvedDate).stream()
                .filter(this::canAccessAttendance)
                .toList();

        return records.stream()
                .sorted(Comparator.comparing(attendance -> attendance.getEmployee().getFullName()))
                .toList();
    }

    @Transactional
    public EmployeeAttendance markEmployeeAbsent(Long employeeId, LocalDate date) {
        User employee = employeeById(employeeId);
        ensureEmployeeAccess(employee);
        LocalDate workDate = date == null ? LocalDate.now() : date;

        return attendanceRepository.findByEmployee_IdAndWorkDate(employee.getId(), workDate)
                .map(existing -> {
                    if (existing.getClockIn() != null) {
                        throw new IllegalArgumentException("Employee already has a clock-in for this date.");
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    EmployeeAttendance attendance = new EmployeeAttendance();
                    attendance.setEmployee(employee);
                    attendance.setShop(employee.getShop());
                    attendance.setShopLocation(employee.getShopLocation());
                    attendance.setWorkDate(workDate);
                    attendance.setStatus(AttendanceStatus.ABSENT);
                    attendance.setAbsenceDecision(AbsenceDecision.UNRESOLVED);
                    attendance.setWorkedHours(BigDecimal.ZERO);
                    return attendanceRepository.save(attendance);
                });
    }

    @Transactional
    public EmployeeAttendance resolveAbsence(Long attendanceId, AbsenceDecision decision, String notes) {
        EmployeeAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with id: " + attendanceId));
        ensureAttendanceAccess(attendance);

        if (decision == null || decision == AbsenceDecision.UNRESOLVED) {
            throw new IllegalArgumentException("Choose a final absence decision.");
        }

        if (attendance.getStatus() != AttendanceStatus.ABSENT) {
            throw new IllegalArgumentException("Only absent attendance records can be resolved as absences.");
        }

        attendance.setAbsenceDecision(decision);
        attendance.setNotes(notes);
        return attendanceRepository.save(attendance);
    }

    public List<EmployeeAttendance> getUnresolvedAbsences() {
        List<EmployeeAttendance> absences = attendanceRepository.findByStatusAndAbsenceDecision(
                        AttendanceStatus.ABSENT,
                        AbsenceDecision.UNRESOLVED).stream()
                .filter(this::canAccessAttendance)
                .toList();

        return absences.stream()
                .sorted(Comparator
                        .comparing(EmployeeAttendance::getWorkDate).reversed()
                        .thenComparing(attendance -> attendance.getEmployee().getFullName()))
                .toList();
    }

    public List<User> getEmployees() {
        return userRepository.findByRoleOrderByCreatedAtDesc(UserRole.EMPLOYEE).stream()
                .filter(shopContextService::canAccessTenantUser)
                .toList();
    }

    private User currentEmployee() {
        User user = shopContextService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found."));

        if (user.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("Only employees can use clock-in and clock-out.");
        }

        shopContextService.requireShopForAdminOrEmployee();
        return user;
    }

    private User employeeById(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee is required.");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("User with id: " + employeeId + " is not an employee.");
        }

        return employee;
    }

    private void ensureEmployeeAccess(User employee) {
        if (!shopContextService.canAccessTenantUser(employee)) {
            throw new AccessDeniedException("You do not have permission to access this employee.");
        }
    }

    private void ensureAttendanceAccess(EmployeeAttendance attendance) {
        if (!canAccessAttendance(attendance)) {
            throw new AccessDeniedException("You do not have permission to access this attendance record.");
        }
    }

    private boolean canAccessAttendance(EmployeeAttendance attendance) {
        if (attendance.getShopLocation() != null) {
            return shopContextService.canAccessTenantResource(attendance.getShop(), attendance.getShopLocation());
        }

        return shopContextService.canAccessTenantResource(attendance.getShop(), attendance.getShopLocation())
                || (attendance.getEmployee() != null && shopContextService.canAccessTenantUser(attendance.getEmployee()));
    }

    private void validateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
    }

    private BigDecimal calculateWorkedHours(LocalDateTime clockIn, LocalDateTime clockOut) {
        long minutes = Duration.between(clockIn, clockOut).toMinutes();

        if (minutes < 0) {
            throw new IllegalArgumentException("Worked hours cannot be negative.");
        }

        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
