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

import com.aem.tiretrack.dto.accounting.AccountingReport;
import com.aem.tiretrack.dto.accounting.PayExpenseRequest;
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
    public List<AccountingAccount> getAccounts() {
        return accountingService.getAccounts();
    }

    @PostMapping("/accounts")
    public AccountingAccount createAccount(@Valid @RequestBody AccountingAccount account) {
        return accountingService.createAccount(account);
    }

    @GetMapping("/vendors")
    public List<Vendor> getVendors() {
        return accountingService.getVendors();
    }

    @PostMapping("/vendors")
    public Vendor createVendor(@Valid @RequestBody Vendor vendor) {
        return accountingService.createVendor(vendor);
    }

    @GetMapping("/expenses")
    public List<Expense> getExpenses() {
        return accountingService.getExpenses();
    }

    @PostMapping("/expenses")
    public Expense createExpense(@Valid @RequestBody Expense expense) {
        return accountingService.createExpense(expense);
    }

    @PostMapping("/expenses/{id}/pay")
    public Expense payExpense(@PathVariable Long id, @RequestBody(required = false) PayExpenseRequest request) {
        return accountingService.payExpense(id, request == null ? new PayExpenseRequest() : request);
    }

    @GetMapping("/reports")
    public AccountingReport getReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return accountingService.getReport(start, end);
    }
}
