package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.PayrollShiftSlotRequest;
import com.aem.tiretrack.dto.PayrollShiftSlotResponse;
import com.aem.tiretrack.service.PayrollScheduleService;

@RestController
@RequestMapping("/api/payroll/shift-slots")
public class PayrollScheduleController {
    private final PayrollScheduleService payrollScheduleService;

    public PayrollScheduleController(PayrollScheduleService payrollScheduleService) {
        this.payrollScheduleService = payrollScheduleService;
    }

    @GetMapping
    public List<PayrollShiftSlotResponse> getSlots(@RequestParam(required = false) Long periodId) {
        return payrollScheduleService.getSlots(periodId);
    }

    @PostMapping
    public PayrollShiftSlotResponse createSlot(@RequestBody PayrollShiftSlotRequest request) {
        return payrollScheduleService.createSlot(request);
    }

    @DeleteMapping("/{slotId}")
    public void deleteSlot(@PathVariable long slotId) {
        payrollScheduleService.deleteSlot(slotId);
    }

    @PostMapping("/{slotId}/signup")
    public PayrollShiftSlotResponse signup(@PathVariable long slotId) {
        return payrollScheduleService.signup(slotId);
    }

    @DeleteMapping("/{slotId}/signup")
    public PayrollShiftSlotResponse cancelSignup(@PathVariable long slotId) {
        return payrollScheduleService.cancelSignup(slotId);
    }

    @PostMapping("/{slotId}/employees/{employeeId}")
    public PayrollShiftSlotResponse assignEmployee(@PathVariable long slotId, @PathVariable long employeeId) {
        return payrollScheduleService.assignEmployee(slotId, employeeId);
    }

    @DeleteMapping("/{slotId}/signups/{signupId}")
    public PayrollShiftSlotResponse removeSignup(@PathVariable long slotId, @PathVariable long signupId) {
        return payrollScheduleService.removeSignup(slotId, signupId);
    }
}
