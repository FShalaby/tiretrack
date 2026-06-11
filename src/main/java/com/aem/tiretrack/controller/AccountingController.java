package com.aem.tiretrack.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.accounting.AccountingAccountResponse;
import com.aem.tiretrack.dto.accounting.AccountingReport;
import com.aem.tiretrack.dto.accounting.DuplicatePaymentDiagnosticResponse;
import com.aem.tiretrack.dto.accounting.ExpenseResponse;
import com.aem.tiretrack.dto.accounting.PayExpenseRequest;
import com.aem.tiretrack.dto.accounting.VendorResponse;
import com.aem.tiretrack.model.AccountingAccount;
import com.aem.tiretrack.model.Expense;
import com.aem.tiretrack.model.Vendor;
import com.aem.tiretrack.service.AccountingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounting")
public class AccountingController {
    private final AccountingService accountingService;

    public AccountingController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @GetMapping("/accounts")
    public List<AccountingAccountResponse> getAccounts() {
        return accountingService.getAccounts().stream().map(AccountingAccountResponse::new).toList();
    }

    @PostMapping("/accounts")
    public AccountingAccountResponse createAccount(@Valid @RequestBody AccountingAccount account) {
        return new AccountingAccountResponse(accountingService.createAccount(account));
    }

    @GetMapping("/vendors")
    public List<VendorResponse> getVendors() {
        return accountingService.getVendors().stream().map(VendorResponse::new).toList();
    }

    @PostMapping("/vendors")
    public VendorResponse createVendor(@Valid @RequestBody Vendor vendor) {
        return new VendorResponse(accountingService.createVendor(vendor));
    }

    @GetMapping("/expenses")
    public List<ExpenseResponse> getExpenses(@RequestParam(required = false) Long locationId) {
        return accountingService.getExpenses(locationId).stream().map(ExpenseResponse::new).toList();
    }

    @PostMapping("/expenses")
    public ExpenseResponse createExpense(@Valid @RequestBody Expense expense) {
        return new ExpenseResponse(accountingService.createExpense(expense));
    }

    @PostMapping("/expenses/{id}/pay")
    public ExpenseResponse payExpense(@PathVariable Long id, @RequestBody(required = false) PayExpenseRequest request) {
        return new ExpenseResponse(accountingService.payExpense(id, request == null ? new PayExpenseRequest() : request));
    }

    @GetMapping("/reports")
    public AccountingReport getReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long locationId) {
        return accountingService.getReport(start, end, locationId);
    }

    @GetMapping("/diagnostics/duplicate-invoice-payments")
    public List<DuplicatePaymentDiagnosticResponse> getDuplicateInvoicePaymentDiagnostics() {
        return accountingService.findDuplicateInvoicePaymentEntries();
    }
}
