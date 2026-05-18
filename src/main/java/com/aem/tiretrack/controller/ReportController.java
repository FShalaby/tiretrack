package com.aem.tiretrack.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.service.PdfService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final PdfService pdfService;

    public ReportController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/invoices/{id}.pdf")
    public ResponseEntity<byte[]> invoicePdf(@PathVariable Long id) {
        return pdf("invoice-" + id + ".pdf", pdfService.invoicePdf(id));
    }

    @GetMapping("/monthly-sales.pdf")
    public ResponseEntity<byte[]> monthlySalesPdf() {
        return pdf("monthly-sales-report.pdf", pdfService.monthlyReportPdf());
    }

    private ResponseEntity<byte[]> pdf(String filename, byte[] bytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
