package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
import com.aem.tiretrack.dto.EmployeeLoanRequest;
import com.aem.tiretrack.dto.PayrollAdjustmentRequest;
import com.aem.tiretrack.dto.PayrollGenerationResponse;
import com.aem.tiretrack.dto.PayrollRecordNotesRequest;
import com.aem.tiretrack.enums.LoanStatus;
import com.aem.tiretrack.enums.PayrollAdjustmentType;
import com.aem.tiretrack.dto.PayrollPeriodRequest;
import com.aem.tiretrack.enums.PayrollStatus;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.EmployeeAttendance;
import com.aem.tiretrack.model.EmployeeLoan;
import com.aem.tiretrack.model.Expense;
import com.aem.tiretrack.model.PayrollAdjustment;
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollRecord;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkShift;
import com.aem.tiretrack.repository.EmployeeAttendanceRepository;
import com.aem.tiretrack.repository.EmployeeLoanRepository;
import com.aem.tiretrack.repository.PayrollAdjustmentRepository;
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
    private final ShopContextService shopContextService;
    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final AccountingService accountingService;
    private final EmployeeAttendanceRepository attendanceRepository;

    public PayrollService(
            PayrollRecordRepository payrollRecordRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            WorkShiftRepository workShiftRepository,
            UserRepository userRepository,
            ShopContextService shopContextService,
            PayrollAdjustmentRepository payrollAdjustmentRepository,
            EmployeeLoanRepository employeeLoanRepository,
            AccountingService accountingService,
            EmployeeAttendanceRepository attendanceRepository) {
        this.payrollRecordRepository = payrollRecordRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.workShiftRepository = workShiftRepository;
        this.userRepository = userRepository;
        this.shopContextService = shopContextService;
        this.payrollAdjustmentRepository = payrollAdjustmentRepository;
        this.employeeLoanRepository = employeeLoanRepository;
        this.accountingService = accountingService;
        this.attendanceRepository = attendanceRepository;
    }

    public List<PayrollPeriod> getAllPeriods() {
        return payrollPeriodRepository.findAll().stream()
                .filter(period -> shopContextService.canAccessTenantResource(period.getShop(), period.getShopLocation()))
                .toList();
    }

    public PayrollPeriod getPeriodById(long id) {
        PayrollPeriod period = payrollPeriodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll period not found with id: " + id));
        ensurePeriodAccess(period);
        return period;
    }

    public PayrollPeriod createPeriod(PayrollPeriodRequest request) {
        validatePeriodDates(request);
        Shop currentShop = shopContextService.isSuperAdmin() ? null : shopContextService.requireShopForAdminOrEmployee();
        ShopLocation currentLocation = shopContextService.getCurrentTenantLocation().orElse(null);

        boolean overlaps = currentShop == null
                ? payrollPeriodRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        request.getEndDate(),
                        request.getStartDate())
                : payrollPeriodRepository.existsByShop_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        currentShop.getId(),
                        request.getEndDate(),
                        request.getStartDate());

        if (overlaps) {
            throw new IllegalArgumentException("Payroll period overlaps with an existing period");
        }

        PayrollPeriod period = new PayrollPeriod();
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setNotes(request.getNotes());
        period.setShop(currentShop);
        period.setShopLocation(currentLocation);

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
        return payrollRecordRepository.findByPayrollPeriod_Id(periodId).stream()
                .filter(this::canAccessRecord)
                .toList();
    }

    public List<PayrollRecord> getRecordsByEmployeeId(long employeeId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isPayrollManager = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.SUPER_ADMIN;
        if (!isPayrollManager && !currentUser.getId().equals(employeeId)) {
            throw new AccessDeniedException("You do not have permission to access this payroll resource");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
        ensureEmployeeAccess(employee);

        return payrollRecordRepository.findByEmployee_Id(employeeId).stream()
                .filter(this::canAccessRecord)
                .toList();
    }

    public List<User> getPayrollEmployees() {
        return userRepository.findByRoleOrderByCreatedAtDesc(UserRole.EMPLOYEE).stream()
                .filter(shopContextService::canAccessTenantUser)
                .toList();
    }

    public User updateEmployeePayrollSettings(long employeeId, EmployeePayrollSettingsRequest request) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("User with id: " + employeeId + " is not an employee");
        }
        ensureEmployeeAccess(employee);

        employee.setPayrollEnabled(request.isPayrollEnabled());
        employee.setHourlyRate(request.getHourlyRate());
        employee.setEmploymentType(request.getEmploymentType());
        return userRepository.save(employee);
    }

    public PayrollRecord getRecordById(long recordId) {
        PayrollRecord record = payrollRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found with id: " + recordId));
        ensureRecordAccess(record);
        return record;
    }

    @Transactional
    public PayrollRecord approveRecord(long recordId) {
        PayrollRecord record = getRecordById(recordId);

        if (record.getStatus() != PayrollStatus.PENDING) {
            throw new IllegalArgumentException("Only pending payroll records can be approved");
        }

        recalculatePayroll(record);
        record.setStatus(PayrollStatus.APPROVED);
        return payrollRecordRepository.save(record);
    }

    @Transactional
    public PayrollRecord cancelRecord(long recordId) {
        PayrollRecord record = getRecordById(recordId);

        if (record.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Paid payroll records cannot be cancelled");
        }
        if (record.getStatus() == PayrollStatus.CANCELLED) {
            throw new IllegalArgumentException("Payroll record is already cancelled");
        }

        record.setStatus(PayrollStatus.CANCELLED);
        return payrollRecordRepository.save(record);
    }

    @Transactional
    public PayrollRecord payRecord(long recordId) {
        PayrollRecord record = getRecordById(recordId);

        if (record.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Payroll record is already paid");
        }
        if (record.getStatus() != PayrollStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved payroll records can be paid");
        }

        recalculatePayroll(record);

        record.setStatus(PayrollStatus.PAID);
        if (record.getPaidAt() == null) {
            record.setPaidAt(LocalDateTime.now());
        }

        if (!record.isAccountingSynced()) {
            applyLoanDeductions(record);
            Expense payrollExpense = accountingService.recordPayrollCost(record);
            record.setAccountingSynced(true);
            record.setAccountingEntryId(payrollExpense.getId());
        }

        return payrollRecordRepository.save(record);
    }

    @Transactional
    public PayrollRecord updateRecordNotes(long recordId, PayrollRecordNotesRequest request) {
        PayrollRecord record = getRecordById(recordId);
        requirePending(record);
        record.setNotes(request == null ? null : request.getNotes());
        recalculatePayroll(record);
        return payrollRecordRepository.save(record);
    }

    @Transactional
    public PayrollRecord addAdjustment(long recordId, PayrollAdjustmentRequest request) {
        PayrollRecord record = getRecordById(recordId);
        requirePending(record);
        validateAdjustmentRequest(request);

        PayrollAdjustment adjustment = new PayrollAdjustment();
        adjustment.setPayrollRecord(record);
        adjustment.setType(request.getType());
        adjustment.setAmount(amount(request.getAmount()));
        adjustment.setNotes(request.getNotes());

        if (request.getEmployeeLoanId() != null) {
            if (request.getType() != PayrollAdjustmentType.LOAN_DEDUCTION) {
                throw new IllegalArgumentException("A loan can only be linked to a LOAN_DEDUCTION adjustment");
            }
            EmployeeLoan loan = employeeLoanRepository.findById(request.getEmployeeLoanId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee loan not found with id: " + request.getEmployeeLoanId()));
            ensureLoanAccess(loan);
            ensureLoanMatchesRecord(loan, record);
            if (loan.getStatus() != LoanStatus.ACTIVE) {
                throw new IllegalArgumentException("Only active loans can be deducted from payroll");
            }
            if (amount(request.getAmount()).compareTo(amount(loan.getRemainingBalance())) > 0) {
                throw new IllegalArgumentException("Loan deduction cannot exceed remaining loan balance");
            }
            adjustment.setEmployeeLoan(loan);
        }

        payrollAdjustmentRepository.save(adjustment);
        PayrollRecord updatedRecord = getRecordById(recordId);
        recalculatePayroll(updatedRecord);
        return payrollRecordRepository.save(updatedRecord);
    }

    @Transactional
    public PayrollRecord deleteAdjustment(long recordId, long adjustmentId) {
        PayrollRecord record = getRecordById(recordId);
        requirePending(record);

        PayrollAdjustment adjustment = payrollAdjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll adjustment not found with id: " + adjustmentId));

        if (adjustment.getPayrollRecord() == null || !record.getId().equals(adjustment.getPayrollRecord().getId())) {
            throw new IllegalArgumentException("Payroll adjustment does not belong to this record");
        }

        payrollAdjustmentRepository.delete(adjustment);
        PayrollRecord updatedRecord = getRecordById(recordId);
        recalculatePayroll(updatedRecord);
        return payrollRecordRepository.save(updatedRecord);
    }

    public List<EmployeeLoan> getLoans() {
        if (shopContextService.isSuperAdmin()) {
            return employeeLoanRepository.findAll();
        }

        Long shopId = shopContextService.requireShopForAdminOrEmployee().getId();
        return employeeLoanRepository.findByShop_IdOrderByCreatedAtDesc(shopId);
    }

    public List<EmployeeLoan> getEmployeeLoans(long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
        ensureEmployeeAccess(employee);
        return employeeLoanRepository.findByEmployee_IdOrderByCreatedAtDesc(employeeId);
    }

    @Transactional
    public EmployeeLoan createLoan(EmployeeLoanRequest request) {
        if (request == null || request.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee is required");
        }

        BigDecimal originalAmount = amount(request.getOriginalAmount());
        if (originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + request.getEmployeeId()));
        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("User with id: " + request.getEmployeeId() + " is not an employee");
        }
        ensureEmployeeAccess(employee);

        EmployeeLoan loan = new EmployeeLoan();
        loan.setEmployee(employee);
        loan.setShop(employee.getShop());
        loan.setOriginalAmount(originalAmount);
        loan.setRemainingBalance(originalAmount);
        loan.setInstallmentAmount(amount(request.getInstallmentAmount()));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNotes(request.getNotes());
        return employeeLoanRepository.save(loan);
    }

    @Transactional
    public EmployeeLoan cancelLoan(long loanId) {
        EmployeeLoan loan = employeeLoanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Employee loan not found with id: " + loanId));
        ensureLoanAccess(loan);

        if (loan.getStatus() == LoanStatus.PAID_OFF) {
            throw new IllegalArgumentException("Paid-off loans cannot be cancelled");
        }

        loan.setStatus(LoanStatus.CANCELLED);
        return employeeLoanRepository.save(loan);
    }

    @Transactional
    public PayrollGenerationResponse generatePayrollForPeriod(long periodId) {
        PayrollPeriod period = getPeriodById(periodId);

        if (period.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException("Cannot generate payroll for a paid period");
        }

        Map<Long, Map<LocalDate, BigDecimal>> hoursByEmployeeDate = new HashMap<>();
        Map<Long, User> employeesById = new HashMap<>();

        collectWorkShiftHours(period, hoursByEmployeeDate, employeesById);
        collectAttendanceHours(period, hoursByEmployeeDate, employeesById);

        List<PayrollRecord> createdRecords = new ArrayList<>();
        List<String> skippedReasons = new ArrayList<>();
        List<User> candidateEmployees = getPayrollEmployees().stream()
                .filter(employee -> userMatchesPeriodContext(employee, period.getShop(), period.getShopLocation()))
                .toList();

        if (candidateEmployees.isEmpty()) {
            skippedReasons.add("No employees are visible for this shop/location.");
        }

        for (User employee : candidateEmployees) {
            Map<LocalDate, BigDecimal> employeeHours = hoursByEmployeeDate.getOrDefault(employee.getId(), Map.of());

            if (!employee.isPayrollEnabled()) {
                skippedReasons.add(employee.getFullName() + ": payroll is disabled.");
                continue;
            }

            if (payrollRecordRepository.existsByPayrollPeriod_IdAndEmployee_Id(
                    period.getId(),
                    employee.getId())) {
                skippedReasons.add(employee.getFullName() + ": payroll record already exists for this period.");
                continue;
            }

            BigDecimal totalWorkedHours = employeeHours.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalWorkedHours.compareTo(BigDecimal.ZERO) <= 0) {
                skippedReasons.add(employee.getFullName() + ": no completed attendance or work shift hours in this period.");
                continue;
            }

            BigDecimal hourlyRate = employee.getHourlyRate();

            if (hourlyRate == null) {
                skippedReasons.add(employee.getFullName() + ": hourly rate is missing.");
                continue;
            }

            PayrollRecord record = new PayrollRecord();
            record.setEmployee(employee);
            record.setPayrollPeriod(period);
            record.setRegularHours(totalWorkedHours);
            record.setOvertimeHours(BigDecimal.ZERO);
            record.setHourlyRate(hourlyRate);
            record.setStatus(PayrollStatus.PENDING);
            recalculatePayroll(record);

            createdRecords.add(payrollRecordRepository.save(record));
        }

        return new PayrollGenerationResponse(createdRecords, skippedReasons);
    }

    private void collectWorkShiftHours(
            PayrollPeriod period,
            Map<Long, Map<LocalDate, BigDecimal>> hoursByEmployeeDate,
            Map<Long, User> employeesById) {
        List<WorkShift> shifts = workShiftRepository.findByShiftDateBetween(
                period.getStartDate(),
                period.getEndDate());

        for (WorkShift shift : shifts) {
            if (shift.getWorkedHours() == null || shift.getEmployee() == null || shift.getShiftDate() == null) {
                continue;
            }

            if (!userMatchesPeriodContext(shift.getEmployee(), period.getShop(), period.getShopLocation())) {
                continue;
            }

            BigDecimal workedHours = amount(shift.getWorkedHours());
            if (workedHours.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            addPayrollHours(hoursByEmployeeDate, employeesById, shift.getEmployee(), shift.getShiftDate(), workedHours, false);
        }
    }

    private void collectAttendanceHours(
            PayrollPeriod period,
            Map<Long, Map<LocalDate, BigDecimal>> hoursByEmployeeDate,
            Map<Long, User> employeesById) {
        List<EmployeeAttendance> attendanceRecords = attendanceRepository.findByWorkDateBetween(
                period.getStartDate(),
                period.getEndDate());

        for (EmployeeAttendance attendance : attendanceRecords) {
            if (attendance.getEmployee() == null
                    || attendance.getWorkDate() == null
                    || attendance.getClockIn() == null
                    || attendance.getClockOut() == null) {
                continue;
            }

            if (!attendanceMatchesPeriodContext(attendance, period.getShop(), period.getShopLocation())) {
                continue;
            }

            BigDecimal workedHours = amount(attendance.getWorkedHours());
            if (workedHours.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Attendance is the live employee clock source. If both attendance and a
            // manually-entered WorkShift exist for the same employee/day, attendance wins.
            addPayrollHours(hoursByEmployeeDate, employeesById, attendance.getEmployee(), attendance.getWorkDate(), workedHours, true);
        }
    }

    private boolean attendanceMatchesPeriodContext(EmployeeAttendance attendance, Shop periodShop, ShopLocation periodLocation) {
        if (periodShop == null) {
            return shopContextService.isSuperAdmin();
        }

        boolean attendanceShopMatches = attendance.getShop() != null
                && periodShop.getId().equals(attendance.getShop().getId());
        boolean employeeShopMatches = attendance.getEmployee() != null
                && attendance.getEmployee().getShop() != null
                && periodShop.getId().equals(attendance.getEmployee().getShop().getId());

        if (!attendanceShopMatches && !employeeShopMatches) {
            return false;
        }

        if (periodLocation == null) {
            return true;
        }

        boolean attendanceLocationMatches = attendance.getShopLocation() != null
                && periodLocation.getId().equals(attendance.getShopLocation().getId());
        boolean employeeLocationMatches = attendance.getEmployee() != null
                && attendance.getEmployee().getShopLocation() != null
                && periodLocation.getId().equals(attendance.getEmployee().getShopLocation().getId());

        return attendanceLocationMatches || employeeLocationMatches;
    }

    private boolean userMatchesPeriodContext(User user, Shop periodShop, ShopLocation periodLocation) {
        boolean shopMatches = periodShop == null
                ? shopContextService.isSuperAdmin()
                : user.getShop() != null && periodShop.getId().equals(user.getShop().getId());

        if (!shopMatches) {
            return false;
        }

        return periodLocation == null
                || user.getShopLocation() == null
                || periodLocation.getId().equals(user.getShopLocation().getId());
    }

    private void addPayrollHours(
            Map<Long, Map<LocalDate, BigDecimal>> hoursByEmployeeDate,
            Map<Long, User> employeesById,
            User employee,
            LocalDate workDate,
            BigDecimal workedHours,
            boolean replaceExistingDay) {
        Long employeeId = employee.getId();
        employeesById.put(employeeId, employee);

        Map<LocalDate, BigDecimal> hoursByDate = hoursByEmployeeDate.computeIfAbsent(employeeId, key -> new HashMap<>());
        if (replaceExistingDay) {
            hoursByDate.put(workDate, workedHours);
            return;
        }

        hoursByDate.merge(workDate, workedHours, BigDecimal::add);
    }

    private void validatePeriodDates(PayrollPeriodRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    private void ensurePeriodAccess(PayrollPeriod period) {
        if (!shopContextService.canAccessTenantResource(period.getShop(), period.getShopLocation())) {
            throw new AccessDeniedException("You do not have permission to access this payroll period.");
        }
    }

    private void ensureEmployeeAccess(User employee) {
        if (!shopContextService.canAccessTenantUser(employee)) {
            throw new AccessDeniedException("You do not have permission to access this employee.");
        }
    }

    private void ensureRecordAccess(PayrollRecord record) {
        if (!canAccessRecord(record)) {
            throw new AccessDeniedException("You do not have permission to access this payroll record.");
        }
    }

    private boolean canAccessRecord(PayrollRecord record) {
        boolean employeeMatches = record.getEmployee() != null
                && shopContextService.canAccessTenantUser(record.getEmployee());
        boolean periodMatches = record.getPayrollPeriod() != null
                && shopContextService.canAccessTenantResource(
                        record.getPayrollPeriod().getShop(),
                        record.getPayrollPeriod().getShopLocation());

        return employeeMatches || periodMatches;
    }

    private void requirePending(PayrollRecord record) {
        if (record.getStatus() != PayrollStatus.PENDING) {
            throw new IllegalArgumentException("Only pending payroll records can be edited");
        }
    }

    private void validateAdjustmentRequest(PayrollAdjustmentRequest request) {
        if (request == null || request.getType() == null) {
            throw new IllegalArgumentException("Adjustment type is required");
        }

        if (request.getAmount() == null || amount(request.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Adjustment amount must be greater than zero");
        }
    }

    private void recalculatePayroll(PayrollRecord record) {
        BigDecimal regularHours = amount(record.getRegularHours());
        BigDecimal overtimeHours = amount(record.getOvertimeHours());
        BigDecimal hourlyRate = amount(record.getHourlyRate());
        BigDecimal overtimeRate = hourlyRate.multiply(new BigDecimal("1.5"));
        BigDecimal bonusAmount = BigDecimal.ZERO;
        BigDecimal reimbursementAmount = BigDecimal.ZERO;
        BigDecimal deductionAmount = BigDecimal.ZERO;
        BigDecimal penaltyAmount = BigDecimal.ZERO;
        BigDecimal loanDeductionAmount = BigDecimal.ZERO;
        BigDecimal taxDeductionAmount = BigDecimal.ZERO;
        BigDecimal otherDeductionAmount = BigDecimal.ZERO;

        for (PayrollAdjustment adjustment : record.getAdjustments()) {
            BigDecimal adjustmentAmount = amount(adjustment.getAmount());
            if (adjustment.getType() == PayrollAdjustmentType.BONUS) {
                bonusAmount = bonusAmount.add(adjustmentAmount);
            } else if (adjustment.getType() == PayrollAdjustmentType.REIMBURSEMENT) {
                reimbursementAmount = reimbursementAmount.add(adjustmentAmount);
            } else if (adjustment.getType() == PayrollAdjustmentType.DEDUCTION) {
                deductionAmount = deductionAmount.add(adjustmentAmount);
            } else if (adjustment.getType() == PayrollAdjustmentType.PENALTY) {
                penaltyAmount = penaltyAmount.add(adjustmentAmount);
            } else if (adjustment.getType() == PayrollAdjustmentType.LOAN_DEDUCTION) {
                loanDeductionAmount = loanDeductionAmount.add(adjustmentAmount);
            } else if (adjustment.getType() == PayrollAdjustmentType.TAX_DEDUCTION) {
                taxDeductionAmount = taxDeductionAmount.add(adjustmentAmount);
            } else if (adjustment.getType() == PayrollAdjustmentType.OTHER) {
                otherDeductionAmount = otherDeductionAmount.add(adjustmentAmount);
            }
        }

        BigDecimal overtimePay = overtimeHours.multiply(overtimeRate);
        BigDecimal grossPay = regularHours.multiply(hourlyRate)
                .add(overtimePay)
                .add(bonusAmount)
                .add(reimbursementAmount);
        BigDecimal totalDeductions = deductionAmount
                .add(penaltyAmount)
                .add(loanDeductionAmount)
                .add(taxDeductionAmount)
                .add(otherDeductionAmount);

        record.setRegularHours(regularHours);
        record.setOvertimeHours(overtimeHours);
        record.setHourlyRate(hourlyRate);
        record.setBonusAmount(amount(bonusAmount));
        record.setReimbursementAmount(amount(reimbursementAmount));
        record.setDeductionAmount(amount(deductionAmount));
        record.setPenaltyAmount(amount(penaltyAmount));
        record.setLoanDeductionAmount(amount(loanDeductionAmount));
        record.setTaxDeductionAmount(amount(taxDeductionAmount));
        record.setOtherDeductionAmount(amount(otherDeductionAmount));
        record.setGrossPay(amount(grossPay));
        record.setTotalDeductions(amount(totalDeductions));
        record.setNetPay(amount(grossPay.subtract(totalDeductions)));
    }

    private void applyLoanDeductions(PayrollRecord record) {
        for (PayrollAdjustment adjustment : record.getAdjustments()) {
            if (adjustment.getType() != PayrollAdjustmentType.LOAN_DEDUCTION || adjustment.getEmployeeLoan() == null) {
                continue;
            }

            EmployeeLoan loan = adjustment.getEmployeeLoan();
            ensureLoanMatchesRecord(loan, record);
            BigDecimal remainingBalance = amount(loan.getRemainingBalance()).subtract(amount(adjustment.getAmount()));

            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                loan.setRemainingBalance(BigDecimal.ZERO);
                loan.setStatus(LoanStatus.PAID_OFF);
            } else {
                loan.setRemainingBalance(amount(remainingBalance));
            }

            employeeLoanRepository.save(loan);
        }
    }

    private void ensureLoanAccess(EmployeeLoan loan) {
        if (!shopContextService.canAccessTenantShop(loan.getShop())) {
            throw new AccessDeniedException("You do not have permission to access this employee loan.");
        }
    }

    private void ensureLoanMatchesRecord(EmployeeLoan loan, PayrollRecord record) {
        if (loan.getEmployee() == null
                || record.getEmployee() == null
                || !loan.getEmployee().getId().equals(record.getEmployee().getId())) {
            throw new IllegalArgumentException("Loan does not belong to this payroll employee");
        }
    }

    private BigDecimal amount(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
