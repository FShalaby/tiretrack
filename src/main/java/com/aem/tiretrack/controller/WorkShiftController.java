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
import com.aem.tiretrack.dto.WorkShiftResponse;
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
    public List<WorkShiftResponse> getAllShifts() {
        return workShiftService.getAllShifts().stream().map(WorkShiftResponse::new).toList();
    }

    @GetMapping("/{id}")
    public WorkShiftResponse getShiftById(@PathVariable long id) {
        return new WorkShiftResponse(workShiftService.getShiftById(id));
    }

    @PostMapping
    public WorkShiftResponse createShift(@Valid @RequestBody WorkShiftRequest request) {
        return new WorkShiftResponse(workShiftService.createShift(request));
    }

    @PutMapping("/{id}")
    public WorkShiftResponse updateShift(@PathVariable long id, @Valid @RequestBody WorkShiftRequest request) {
        return new WorkShiftResponse(workShiftService.updateShift(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable long id) {
        workShiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    public List<WorkShiftResponse> getShiftsByEmployeeId(@PathVariable long employeeId) {
        return workShiftService.getShiftsByEmployeeId(employeeId).stream().map(WorkShiftResponse::new).toList();
    }

    @GetMapping("/employee/{employeeId}/range")
    public List<WorkShiftResponse> getShiftsByEmployeeIdAndDateRange(
            @PathVariable long employeeId,
            @RequestParam @Valid String start,
            @RequestParam @Valid String end) {
        return workShiftService.getShiftsByEmployeeIdAndDateRange(employeeId, start, end).stream()
                .map(WorkShiftResponse::new)
                .toList();
    }

   
}
