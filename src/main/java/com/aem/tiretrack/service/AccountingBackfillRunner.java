package com.aem.tiretrack.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.aem.tiretrack.repository.InvoiceRepository;

@Component
public class AccountingBackfillRunner implements CommandLineRunner {
    private final InvoiceRepository invoiceRepository;
    private final AccountingService accountingService;

    public AccountingBackfillRunner(InvoiceRepository invoiceRepository, AccountingService accountingService) {
        this.invoiceRepository = invoiceRepository;
        this.accountingService = accountingService;
    }

    @Override
    public void run(String... args) {
        invoiceRepository.findAll().forEach(invoice -> {
            try {
                accountingService.recordInvoiceIssued(invoice);
                if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
                    accountingService.recordInvoicePayment(invoice);
                }
            } catch (RuntimeException exception) {
                System.err.println("Skipped accounting backfill for invoice #" + invoice.getId() + ": " + exception.getMessage());
            }
        });
    }
}
