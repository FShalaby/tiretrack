package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.TireRequestCreateRequest;
import com.aem.tiretrack.dto.TireRequestResponse;
import com.aem.tiretrack.dto.TireRequestStatusUpdateRequest;
import com.aem.tiretrack.service.TireRequestService;

@RestController
@RequestMapping("/api/tire-requests")
public class TireRequestController {
    private final TireRequestService tireRequestService;

    public TireRequestController(TireRequestService tireRequestService) {
        this.tireRequestService = tireRequestService;
    }

    @GetMapping
    public List<TireRequestResponse> getAll() {
        return tireRequestService.getVisibleRequests().stream()
                .map(TireRequestResponse::new)
                .toList();
    }

    @PostMapping
    public TireRequestResponse create(@RequestBody TireRequestCreateRequest request) {
        return new TireRequestResponse(tireRequestService.createFromStaff(request));
    }

    @PutMapping("/{id}/status")
    public TireRequestResponse updateStatus(@PathVariable Long id, @RequestBody TireRequestStatusUpdateRequest request) {
        return new TireRequestResponse(tireRequestService.updateStatus(id, request));
    }

    @PostMapping("/{id}/confirm-appointment")
    public TireRequestResponse confirmAppointment(@PathVariable Long id) {
        return new TireRequestResponse(tireRequestService.confirmRelatedAppointment(id));
    }
}
