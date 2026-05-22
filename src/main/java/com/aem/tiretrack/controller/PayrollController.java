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
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollRecord;
import com.aem.tiretrack.model.User;
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
    public List<PayrollPeriod> getPayrollPeriods() {
        return payrollService.getAllPeriods();
    }

    @GetMapping("/periods/{id}")
    public PayrollPeriod getPayrollPeriodById(@PathVariable long id) {
        return payrollService.getPeriodById(id);
    }

    @PostMapping("/periods")
    public PayrollPeriod createPayrollPeriod(@Valid @RequestBody PayrollPeriodRequest request) {
        return payrollService.createPeriod(request);
    }

    @PutMapping("/periods/{id}")
    public PayrollPeriod updatePayrollPeriod(
            @PathVariable long id,
            @Valid @RequestBody PayrollPeriodRequest request) {
        return payrollService.updatePeriod(id, request);
    }

    @DeleteMapping("/periods/{id}")
    public void deletePayrollPeriod(@PathVariable long id) {
        payrollService.deletePeriod(id);
    }

    @PostMapping("/periods/{id}/generate")
    public List<PayrollRecord> generatePayroll(@PathVariable long id) {
        return payrollService.generatePayrollForPeriod(id);
    }

    @GetMapping("/periods/{id}/records")
    public List<PayrollRecord> getPayrollRecordsForPeriod(@PathVariable long id) {
        return payrollService.getRecordsByPeriodId(id);
    }

    @GetMapping("/employees/{employeeId}/records")
    public List<PayrollRecord> getPayrollRecordsForEmployee(@PathVariable long employeeId) {
        return payrollService.getRecordsByEmployeeId(employeeId);
    }

    @GetMapping("/employees")
    public List<User> getPayrollEmployees() {
        return payrollService.getPayrollEmployees();
    }

    @PutMapping("/employees/{employeeId}/settings")
    public User updateEmployeePayrollSettings(
            @PathVariable long employeeId,
            @RequestBody EmployeePayrollSettingsRequest request) {
        return payrollService.updateEmployeePayrollSettings(employeeId, request);
    }

    @PostMapping("/records/{id}/approve")
    public PayrollRecord approvePayrollRecord(@PathVariable long id) {
        return payrollService.approveRecord(id);
    }

    @PostMapping("/records/{id}/pay")
    public PayrollRecord payPayrollRecord(@PathVariable long id) {
        return payrollService.payRecord(id);
    }

    @PostMapping("/records/{id}/cancel")
    public PayrollRecord cancelPayrollRecord(@PathVariable long id) {
        return payrollService.cancelRecord(id);
    }
}
