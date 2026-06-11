package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.InvoiceResponse;
import com.aem.tiretrack.dto.WorkOrderRequest;
import com.aem.tiretrack.dto.WorkOrderResponse;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.service.ShopContextService;
import com.aem.tiretrack.service.WorkOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {
    private final WorkOrderService workOrderService;
    private final ShopContextService shopContextService;

    public WorkOrderController(WorkOrderService workOrderService, ShopContextService shopContextService) {
        this.workOrderService = workOrderService;
        this.shopContextService = shopContextService;
    }

    @GetMapping
    public List<WorkOrderResponse> getAllWorkOrders(@org.springframework.web.bind.annotation.RequestParam(required = false) Long locationId) {
        ShopLocation location = shopContextService.resolveAccessibleLocation(locationId, null, false).orElse(null);
        return workOrderService.getAllWorkOrders().stream()
                .filter(workOrder -> matchesLocation(workOrder.getShopLocation(), location))
                .map(WorkOrderResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public WorkOrderResponse getWorkOrderById(@PathVariable Long id) {
        return new WorkOrderResponse(workOrderService.getWorkOrderById(id));
    }

    @PostMapping
    public WorkOrderResponse createWorkOrder(@Valid @RequestBody WorkOrderRequest request) {
        return new WorkOrderResponse(workOrderService.createWorkOrder(request));
    }

    @PostMapping("/from-appointment/{appointmentId}")
    public WorkOrderResponse createFromAppointment(@PathVariable Long appointmentId) {
        return new WorkOrderResponse(workOrderService.createFromAppointment(appointmentId));
    }

    @PutMapping("/{id}")
    public WorkOrderResponse updateWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderRequest request) {
        return new WorkOrderResponse(workOrderService.updateWorkOrder(id, request));
    }

    @PostMapping("/{id}/start")
    public WorkOrderResponse startWorkOrder(@PathVariable Long id) {
        return new WorkOrderResponse(workOrderService.startWorkOrder(id));
    }

    @PostMapping("/{id}/vehicle-ready")
    public WorkOrderResponse markVehicleReady(@PathVariable Long id) {
        return new WorkOrderResponse(workOrderService.markVehicleReady(id));
    }

    @PostMapping("/{id}/complete")
    public WorkOrderResponse completeWorkOrder(@PathVariable Long id) {
        return new WorkOrderResponse(workOrderService.completeWorkOrder(id));
    }

    @PostMapping("/{id}/cancel")
    public WorkOrderResponse cancelWorkOrder(@PathVariable Long id) {
        return new WorkOrderResponse(workOrderService.cancelWorkOrder(id));
    }

    @GetMapping("/{id}/invoice-preview")
    public InvoiceResponse previewInvoice(@PathVariable Long id) {
        return new InvoiceResponse(workOrderService.previewInvoice(id));
    }

    @PostMapping("/{id}/convert-to-invoice")
    public InvoiceResponse convertToInvoice(@PathVariable Long id) {
        return new InvoiceResponse(workOrderService.convertToInvoice(id));
    }

    private boolean matchesLocation(ShopLocation resourceLocation, ShopLocation requestedLocation) {
        if (requestedLocation == null) {
            return true;
        }

        return resourceLocation != null && requestedLocation.getId().equals(resourceLocation.getId());
    }
}
