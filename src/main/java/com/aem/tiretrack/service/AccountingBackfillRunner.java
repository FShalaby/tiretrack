package com.aem.tiretrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.JournalEntryRepository;

@Component
@ConditionalOnProperty(name = {"app.backfill.enabled", "app.backfill.accounting.enabled"}, havingValue = "true")
public class AccountingBackfillRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AccountingBackfillRunner.class);

    private final InvoiceRepository invoiceRepository;
    private final AccountingService accountingService;
    private final JournalEntryRepository journalEntryRepository;

    public AccountingBackfillRunner(
            InvoiceRepository invoiceRepository,
            AccountingService accountingService,
            JournalEntryRepository journalEntryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.accountingService = accountingService;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public void run(String... args) {
        invoiceRepository.findAll().forEach(invoice -> {
            try {
                accountingService.recordInvoiceIssued(invoice);
                if ("PAID".equalsIgnoreCase(invoice.getStatus()) && !hasPaymentEntry(invoice.getId())) {
                    accountingService.recordInvoicePayment(invoice);
                }
            } catch (RuntimeException exception) {
                log.warn("Skipped accounting backfill for invoice #{}: {}", invoice.getId(), exception.getMessage());
            }
        });
    }

    private boolean hasPaymentEntry(Long invoiceId) {
        return invoiceId != null
                && journalEntryRepository.countByReferenceTypeAndReferenceIdAndSourceStartingWith(
                        "Invoice",
                        invoiceId,
                        "INVOICE_PAYMENT") > 0;
    }
}
