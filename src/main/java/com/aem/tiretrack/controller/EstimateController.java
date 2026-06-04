package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.EstimateRequest;
import com.aem.tiretrack.dto.EstimateResponse;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.service.EstimateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estimates")
public class EstimateController {
    private final EstimateService estimateService;

    public EstimateController(EstimateService estimateService) {
        this.estimateService = estimateService;
    }

    @GetMapping
    public List<EstimateResponse> getAllEstimates() {
        return estimateService.getAllEstimates().stream()
                .map(EstimateResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public EstimateResponse getEstimateById(@PathVariable Long id) {
        return new EstimateResponse(estimateService.getEstimateById(id));
    }

    @PostMapping
    public EstimateResponse createEstimate(@Valid @RequestBody EstimateRequest request) {
        return new EstimateResponse(estimateService.createEstimate(request));
    }

    @PutMapping("/{id}")
    public EstimateResponse updateEstimate(
            @PathVariable Long id,
            @Valid @RequestBody EstimateRequest request) {
        return new EstimateResponse(estimateService.updateEstimate(id, request));
    }

    @PostMapping("/{id}/approve")
    public EstimateResponse approveEstimate(@PathVariable Long id) {
        return new EstimateResponse(estimateService.approveEstimate(id));
    }

    @PostMapping("/{id}/send")
    public EstimateResponse sendEstimate(@PathVariable Long id) {
        return new EstimateResponse(estimateService.sendEstimate(id));
    }

    @PostMapping("/{id}/decline")
    public EstimateResponse declineEstimate(@PathVariable Long id) {
        return new EstimateResponse(estimateService.declineEstimate(id));
    }

    @PostMapping("/{id}/cancel")
    public EstimateResponse cancelEstimate(@PathVariable Long id) {
        return new EstimateResponse(estimateService.cancelEstimate(id));
    }

    @PostMapping("/{id}/convert-to-invoice")
    public Invoice convertToInvoice(@PathVariable Long id) {
        return estimateService.convertToInvoice(id);
    }
}
