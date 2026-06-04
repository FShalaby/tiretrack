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

import com.aem.tiretrack.dto.EmployeePayrollSettingsRequest;
import com.aem.tiretrack.dto.EmployeeLoanRequest;
import com.aem.tiretrack.dto.EmployeeLoanResponse;
import com.aem.tiretrack.dto.PayrollAdjustmentRequest;
import com.aem.tiretrack.dto.PayrollGenerationResponse;
import com.aem.tiretrack.dto.PayrollPeriodRequest;
import com.aem.tiretrack.dto.PayrollPeriodResponse;
import com.aem.tiretrack.dto.PayrollRecordNotesRequest;
import com.aem.tiretrack.dto.PayrollRecordResponse;
import com.aem.tiretrack.dto.UserResponse;
import com.aem.tiretrack.service.PayrollService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/periods")
    public List<PayrollPeriodResponse> getPayrollPeriods() {
        return payrollService.getAllPeriods().stream().map(PayrollPeriodResponse::new).toList();
    }

    @GetMapping("/periods/{id}")
    public PayrollPeriodResponse getPayrollPeriodById(@PathVariable long id) {
        return new PayrollPeriodResponse(payrollService.getPeriodById(id));
    }

    @PostMapping("/periods")
    public PayrollPeriodResponse createPayrollPeriod(@Valid @RequestBody PayrollPeriodRequest request) {
        return new PayrollPeriodResponse(payrollService.createPeriod(request));
    }

    @PutMapping("/periods/{id}")
    public PayrollPeriodResponse updatePayrollPeriod(
            @PathVariable long id,
            @Valid @RequestBody PayrollPeriodRequest request) {
        return new PayrollPeriodResponse(payrollService.updatePeriod(id, request));
    }

    @DeleteMapping("/periods/{id}")
    public void deletePayrollPeriod(@PathVariable long id) {
        payrollService.deletePeriod(id);
    }

    @PostMapping("/periods/{id}/generate")
    public PayrollGenerationResponse generatePayroll(@PathVariable long id) {
        return payrollService.generatePayrollForPeriod(id);
    }

    @GetMapping("/periods/{id}/records")
    public List<PayrollRecordResponse> getPayrollRecordsForPeriod(@PathVariable long id) {
        return payrollService.getRecordsByPeriodId(id).stream().map(PayrollRecordResponse::new).toList();
    }

    @GetMapping("/employees/{employeeId}/records")
    public List<PayrollRecordResponse> getPayrollRecordsForEmployee(@PathVariable long employeeId) {
        return payrollService.getRecordsByEmployeeId(employeeId).stream().map(PayrollRecordResponse::new).toList();
    }

    @GetMapping("/employees")
    public List<UserResponse> getPayrollEmployees() {
        return payrollService.getPayrollEmployees().stream().map(UserResponse::new).toList();
    }

    @PutMapping("/employees/{employeeId}/settings")
    public UserResponse updateEmployeePayrollSettings(
            @PathVariable long employeeId,
            @RequestBody EmployeePayrollSettingsRequest request) {
        return new UserResponse(payrollService.updateEmployeePayrollSettings(employeeId, request));
    }

    @PostMapping("/records/{id}/approve")
    public PayrollRecordResponse approvePayrollRecord(@PathVariable long id) {
        return new PayrollRecordResponse(payrollService.approveRecord(id));
    }

    @PostMapping("/records/{id}/pay")
    public PayrollRecordResponse payPayrollRecord(@PathVariable long id) {
        return new PayrollRecordResponse(payrollService.payRecord(id));
    }

    @PutMapping("/records/{id}/notes")
    public PayrollRecordResponse updatePayrollRecordNotes(
            @PathVariable long id,
            @RequestBody PayrollRecordNotesRequest request) {
        return new PayrollRecordResponse(payrollService.updateRecordNotes(id, request));
    }

    @PostMapping("/records/{id}/adjustments")
    public PayrollRecordResponse addPayrollAdjustment(
            @PathVariable long id,
            @RequestBody PayrollAdjustmentRequest request) {
        return new PayrollRecordResponse(payrollService.addAdjustment(id, request));
    }

    @DeleteMapping("/records/{recordId}/adjustments/{adjustmentId}")
    public PayrollRecordResponse deletePayrollAdjustment(
            @PathVariable long recordId,
            @PathVariable long adjustmentId) {
        return new PayrollRecordResponse(payrollService.deleteAdjustment(recordId, adjustmentId));
    }

    @PostMapping("/records/{id}/cancel")
    public PayrollRecordResponse cancelPayrollRecord(@PathVariable long id) {
        return new PayrollRecordResponse(payrollService.cancelRecord(id));
    }

    @GetMapping("/loans")
    public List<EmployeeLoanResponse> getEmployeeLoans() {
        return payrollService.getLoans().stream().map(EmployeeLoanResponse::new).toList();
    }

    @GetMapping("/employees/{employeeId}/loans")
    public List<EmployeeLoanResponse> getEmployeeLoansForEmployee(@PathVariable long employeeId) {
        return payrollService.getEmployeeLoans(employeeId).stream().map(EmployeeLoanResponse::new).toList();
    }

    @PostMapping("/loans")
    public EmployeeLoanResponse createEmployeeLoan(@RequestBody EmployeeLoanRequest request) {
        return new EmployeeLoanResponse(payrollService.createLoan(request));
    }

    @PostMapping("/loans/{id}/cancel")
    public EmployeeLoanResponse cancelEmployeeLoan(@PathVariable long id) {
        return new EmployeeLoanResponse(payrollService.cancelLoan(id));
    }
}
