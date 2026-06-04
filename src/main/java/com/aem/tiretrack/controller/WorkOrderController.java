package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.WorkOrderRequest;
import com.aem.tiretrack.dto.WorkOrderResponse;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.service.WorkOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {
    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public List<WorkOrderResponse> getAllWorkOrders() {
        return workOrderService.getAllWorkOrders().stream()
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
    public Invoice previewInvoice(@PathVariable Long id) {
        return workOrderService.previewInvoice(id);
    }

    @PostMapping("/{id}/convert-to-invoice")
    public Invoice convertToInvoice(@PathVariable Long id) {
        return workOrderService.convertToInvoice(id);
    }
}
