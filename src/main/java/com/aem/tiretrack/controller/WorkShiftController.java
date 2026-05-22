package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.WorkShiftRequest;
import com.aem.tiretrack.model.WorkShift;
import com.aem.tiretrack.service.WorkShiftService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shifts")
public class WorkShiftController {
    private final WorkShiftService workShiftService;

    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    @GetMapping
    public List<WorkShift> getAllShifts() {
        return workShiftService.getAllShifts();
    }

    @GetMapping("/{id}")
    public WorkShift getShiftById(@PathVariable long id) {
        return workShiftService.getShiftById(id);
    }

    @PostMapping
    public WorkShift createShift(@Valid @RequestBody WorkShiftRequest request) {
        return workShiftService.createShift(request);
    }

    @PutMapping("/{id}")
    public WorkShift updateShift(@PathVariable long id, @Valid @RequestBody WorkShiftRequest request) {
        return workShiftService.updateShift(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable long id) {
        workShiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    public List<WorkShift> getShiftsByEmployeeId(@PathVariable long employeeId) {
        return workShiftService.getShiftsByEmployeeId(employeeId);
    }

    @GetMapping("/employee/{employeeId}/range")
    public List<WorkShift> getShiftsByEmployeeIdAndDateRange(
            @PathVariable long employeeId,
            @RequestParam @Valid String start,
            @RequestParam @Valid String end) {
        return workShiftService.getShiftsByEmployeeIdAndDateRange(employeeId, start, end);
    }

   
}
