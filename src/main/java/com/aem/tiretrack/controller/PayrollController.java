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
import com.aem.tiretrack.dto.PayrollPeriodRequest;
import com.aem.tiretrack.dto.PayrollPeriodResponse;
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
    public List<PayrollRecordResponse> generatePayroll(@PathVariable long id) {
        return payrollService.generatePayrollForPeriod(id).stream().map(PayrollRecordResponse::new).toList();
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

    @PostMapping("/records/{id}/cancel")
    public PayrollRecordResponse cancelPayrollRecord(@PathVariable long id) {
        return new PayrollRecordResponse(payrollService.cancelRecord(id));
    }
}
