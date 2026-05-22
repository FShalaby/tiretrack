package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.EmployeePayrollSettingsRequest;
import com.aem.tiretrack.dto.PayrollPeriodRequest;
import com.aem.tiretrack.enums.PayrollStatus;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollRecord;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkShift;
import com.aem.tiretrack.repository.PayrollPeriodRepository;
import com.aem.tiretrack.repository.PayrollRecordRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.repository.WorkShiftRepository;

@Service
public class PayrollService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final WorkShiftRepository workShiftRepository;
    private final UserRepository userRepository;

    public PayrollService(
            PayrollRecordRepository payrollRecordRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            WorkShiftRepository workShiftRepository,
            UserRepository userRepository) {
        this.payrollRecordRepository = payrollRecordRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.workShiftRepository = workShiftRepository;
        this.userRepository = userRepository;
    }

    public List<PayrollPeriod> getAllPeriods() {
        return payrollPeriodRepository.findAll();
    }

    public PayrollPeriod getPeriodById(long id) {
        return payrollPeriodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll period not found with id: " + id));
    }

    public PayrollPeriod createPeriod(PayrollPeriodRequest request) {
        validatePeriodDates(request);

        if (payrollPeriodRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                request.getEndDate(),
                request.getStartDate())) {
            throw new IllegalArgumentException("Payroll period overlaps with an existing period");
        }

        PayrollPeriod period = new PayrollPeriod();
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setNotes(request.getNotes());

        return payrollPeriodRepository.save(period);
    }

    public PayrollPeriod updatePeriod(long id, PayrollPeriodRequest request) {
        validatePeriodDates(request);

        PayrollPeriod period = getPeriodById(id);

        if (period.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Paid payroll periods cannot be edited");
        }

        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setNotes(request.getNotes());

        return payrollPeriodRepository.save(period);
    }

    public void deletePeriod(long id) {
        PayrollPeriod period = getPeriodById(id);

        if (period.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Paid payroll periods cannot be deleted");
        }

        payrollPeriodRepository.delete(period);
    }

    public List<PayrollRecord> getRecordsByPeriodId(long periodId) {
        return payrollRecordRepository.findByPayrollPeriod_Id(periodId);
    }

    public List<PayrollRecord> getRecordsByEmployeeId(long employeeId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (currentUser.getRole() != UserRole.ADMIN && !currentUser.getId().equals(employeeId)) {
            throw new AccessDeniedException("You do not have permission to access this payroll resource");
        }

        return payrollRecordRepository.findByEmployee_Id(employeeId);
    }

    public List<User> getPayrollEmployees() {
        return userRepository.findByRoleOrderByCreatedAtDesc(UserRole.EMPLOYEE);
    }

    public User updateEmployeePayrollSettings(long employeeId, EmployeePayrollSettingsRequest request) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("User with id: " + employeeId + " is not an employee");
        }

        employee.setPayrollEnabled(request.isPayrollEnabled());
        employee.setHourlyRate(request.getHourlyRate());
        employee.setEmploymentType(request.getEmploymentType());
        return userRepository.save(employee);
    }

    public PayrollRecord getRecordById(long recordId) {
        return payrollRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found with id: " + recordId));
    }

    public PayrollRecord approveRecord(long recordId) {
        PayrollRecord record = getRecordById(recordId);

        if (record.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Paid payroll records cannot be approved again");
        }

        record.setStatus(PayrollStatus.APPROVED);
        return payrollRecordRepository.save(record);
    }

    public PayrollRecord cancelRecord(long recordId) {
        PayrollRecord record = getRecordById(recordId);

        if (record.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Paid payroll records cannot be cancelled");
        }

        record.setStatus(PayrollStatus.CANCELLED);
        return payrollRecordRepository.save(record);
    }

    public PayrollRecord payRecord(long recordId) {
        PayrollRecord record = getRecordById(recordId);

        if (record.getStatus() == PayrollStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled payroll records cannot be paid");
        }

        record.setStatus(PayrollStatus.PAID);
        record.setPaidAt(LocalDateTime.now());

        return payrollRecordRepository.save(record);
    }

    @Transactional
    public List<PayrollRecord> generatePayrollForPeriod(long periodId) {
        PayrollPeriod period = getPeriodById(periodId);

        if (period.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Cannot generate payroll for a paid period");
        }

        List<WorkShift> shifts = workShiftRepository.findByShiftDateBetween(
                period.getStartDate(),
                period.getEndDate()
        );

        Map<Long, List<WorkShift>> shiftsByEmployee = new HashMap<>();

        for (WorkShift shift : shifts) {
            if (shift.getWorkedHours() == null) {
                continue;
            }

            Long employeeId = shift.getEmployee().getId();

            shiftsByEmployee
                    .computeIfAbsent(employeeId, key -> new ArrayList<>())
                    .add(shift);
        }

        List<PayrollRecord> createdRecords = new ArrayList<>();

        for (Map.Entry<Long, List<WorkShift>> entry : shiftsByEmployee.entrySet()) {
            List<WorkShift> employeeShifts = entry.getValue();
            WorkShift firstShift = employeeShifts.get(0);

            if (!firstShift.getEmployee().isPayrollEnabled()) {
                continue;
            }

            if (payrollRecordRepository.existsByPayrollPeriod_IdAndEmployee_Id(
                    period.getId(),
                    firstShift.getEmployee().getId())) {
                continue;
            }

            BigDecimal totalWorkedHours = BigDecimal.ZERO;

            for (WorkShift shift : employeeShifts) {
                totalWorkedHours = totalWorkedHours.add(shift.getWorkedHours());
            }

            BigDecimal hourlyRate = firstShift.getEmployee().getHourlyRate();

            if (hourlyRate == null) {
                throw new IllegalArgumentException(
                        "Employee " + firstShift.getEmployee().getFullName() + " does not have an hourly rate"
                );
            }

            PayrollRecord record = new PayrollRecord();
            record.setEmployee(firstShift.getEmployee());
            record.setPayrollPeriod(period);
            record.setRegularHours(totalWorkedHours);
            record.setOvertimeHours(BigDecimal.ZERO);
            record.setHourlyRate(hourlyRate);
            record.setGrossPay(totalWorkedHours.multiply(hourlyRate));
            record.setStatus(PayrollStatus.PENDING);

            createdRecords.add(payrollRecordRepository.save(record));
        }

        return createdRecords;
    }

    private void validatePeriodDates(PayrollPeriodRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }
}
