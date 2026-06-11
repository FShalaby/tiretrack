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
import com.aem.tiretrack.dto.InvoiceResponse;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.service.EstimateService;
import com.aem.tiretrack.service.ShopContextService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estimates")
public class EstimateController {
    private final EstimateService estimateService;
    private final ShopContextService shopContextService;

    public EstimateController(EstimateService estimateService, ShopContextService shopContextService) {
        this.estimateService = estimateService;
        this.shopContextService = shopContextService;
    }

    @GetMapping
    public List<EstimateResponse> getAllEstimates(@org.springframework.web.bind.annotation.RequestParam(required = false) Long locationId) {
        ShopLocation location = shopContextService.resolveAccessibleLocation(locationId, null, false).orElse(null);
        return estimateService.getAllEstimates().stream()
                .filter(estimate -> matchesLocation(estimate.getShopLocation(), location))
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
    public InvoiceResponse convertToInvoice(
            @PathVariable Long id,
            @RequestBody(required = false) Invoice invoiceDraft) {
        return new InvoiceResponse(estimateService.convertToInvoice(id, invoiceDraft));
    }

    private boolean matchesLocation(ShopLocation resourceLocation, ShopLocation requestedLocation) {
        if (requestedLocation == null) {
            return true;
        }

        return resourceLocation != null && requestedLocation.getId().equals(resourceLocation.getId());
    }
}
