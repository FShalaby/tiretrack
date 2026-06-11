package com.aem.tiretrack.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aem.tiretrack.model.CompanySettings;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.InvoiceItem;

@Service
public class PdfService {
    private final CompanySettingsService companySettingsService;
    private final InvoiceService invoiceService;

    public PdfService(CompanySettingsService companySettingsService, InvoiceService invoiceService) {
        this.companySettingsService = companySettingsService;
        this.invoiceService = invoiceService;
    }

    public byte[] invoicePdf(Long invoiceId) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        CompanySettings settings = settingsForInvoice(invoice);
        StringBuilder text = new StringBuilder();

        text.append(settings.getShopName()).append("\n");
        text.append("Invoice #").append(invoice.getId()).append("\n");
        text.append("Customer: ").append(invoice.getCustomerName()).append("\n");
        text.append("Phone: ").append(invoice.getPhone()).append("\n\n");

        for (InvoiceItem item : invoice.getItems()) {
            text.append(item.getItemName()).append("  x").append(item.getQuantity())
                    .append("  $").append(item.getTotalPrice()).append("\n");
        }

        text.append("\nSubtotal: $").append(invoice.getSubtotal());
        text.append("\nTax: $").append(invoice.getTaxAmount());
        text.append("\nTotal: $").append(invoice.getTotal());
        if (settings.getInvoiceTerms() != null && !settings.getInvoiceTerms().isBlank()) {
            text.append("\n\n").append(settings.getInvoiceTerms());
        }

        return simplePdf("Invoice " + invoice.getId(), text.toString());
    }

    public byte[] monthlyReportPdf() {
        LocalDate now = LocalDate.now();
        List<Invoice> invoices = invoiceService.getAllInvoices().stream()
                .filter(invoice -> invoice.getCreatedAt() != null
                        && invoice.getCreatedAt().getMonth() == now.getMonth()
                        && invoice.getCreatedAt().getYear() == now.getYear())
                .toList();
        StringBuilder text = new StringBuilder();
        double revenue = invoices.stream().mapToDouble(invoice -> invoice.getTotal().doubleValue()).sum();

        text.append(companySettingsService.getSettings().getShopName()).append("\n");
        text.append("Monthly Sales Report - ").append(now.format(DateTimeFormatter.ofPattern("MMMM yyyy"))).append("\n\n");
        text.append("Invoices: ").append(invoices.size()).append("\n");
        text.append("Revenue: $").append(String.format("%.2f", revenue)).append("\n\n");

        invoices.forEach(invoice -> text.append("#").append(invoice.getId()).append(" ")
                .append(invoice.getCustomerName()).append(" ")
                .append(invoice.getStatus()).append(" $")
                .append(invoice.getTotal()).append("\n"));

        return simplePdf("Monthly Sales Report", text.toString());
    }

    private CompanySettings settingsForInvoice(Invoice invoice) {
        return invoice.getShop() == null
                ? companySettingsService.getSettings()
                : companySettingsService.getSettings(invoice.getShop().getId());
    }

    private byte[] simplePdf(String title, String text) {
        String safeText = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        String[] lines = safeText.split("\\R");
        StringBuilder content = new StringBuilder("BT /F1 12 Tf 50 760 Td ");

        for (String line : lines) {
            content.append("(").append(line).append(") Tj 0 -18 Td ");
        }

        content.append("ET");
        byte[] stream = content.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String[] objects = {
                "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n",
                "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n",
                "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n",
                "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n",
                "5 0 obj << /Length " + stream.length + " >> stream\n" + content + "\nendstream endobj\n"
        };
        int[] offsets = new int[objects.length + 1];

        try {
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.UTF_8));

            for (int index = 0; index < objects.length; index++) {
                offsets[index + 1] = out.size();
                out.write(objects[index].getBytes(StandardCharsets.UTF_8));
            }

            int xrefOffset = out.size();
            out.write(("xref\n0 " + (objects.length + 1) + "\n").getBytes(StandardCharsets.UTF_8));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.UTF_8));

            for (int index = 1; index < offsets.length; index++) {
                out.write(String.format("%010d 00000 n \n", offsets[index]).getBytes(StandardCharsets.UTF_8));
            }

            out.write(("trailer << /Size " + (objects.length + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF").getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new RuntimeException("Could not generate PDF", exception);
        }

        return out.toByteArray();
    }
}
