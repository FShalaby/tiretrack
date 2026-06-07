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

import com.aem.tiretrack.dto.InvoiceResponse;
import com.aem.tiretrack.dto.InvoiceStatusUpdateRequest;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.service.InvoiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices().stream().map(InvoiceResponse::new).toList();
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceById(@PathVariable Long id) {
        return new InvoiceResponse(invoiceService.getInvoiceById(id));
    }

    @PostMapping
    public InvoiceResponse createInvoice(
            @Valid @RequestBody Invoice invoice) {

        return new InvoiceResponse(invoiceService.saveInvoice(invoice));
    }

    @DeleteMapping("/{id}")
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }

    @PutMapping("/{id}/status")
    public InvoiceResponse updateInvoiceStatus(@PathVariable Long id, @RequestBody InvoiceStatusUpdateRequest request) {
        return new InvoiceResponse(invoiceService.updateInvoiceStatus(id, request));
    }
}
