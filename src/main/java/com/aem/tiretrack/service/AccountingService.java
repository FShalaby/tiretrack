package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aem.tiretrack.dto.accounting.AccountBalance;
import com.aem.tiretrack.dto.accounting.AccountingReport;
import com.aem.tiretrack.dto.accounting.DuplicatePaymentDiagnosticResponse;
import com.aem.tiretrack.dto.accounting.PayExpenseRequest;
import com.aem.tiretrack.enums.AccountingPaymentMethod;
import com.aem.tiretrack.enums.AccountType;
import com.aem.tiretrack.enums.ExpenseCategory;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.enums.VendorCategory;
import com.aem.tiretrack.model.AccountingAccount;
import com.aem.tiretrack.model.Expense;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.JournalEntry;
import com.aem.tiretrack.model.JournalEntryLine;
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollRecord;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.Vendor;
import com.aem.tiretrack.repository.AccountingAccountRepository;
import com.aem.tiretrack.repository.ExpenseRepository;
import com.aem.tiretrack.repository.JournalEntryRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.repository.VendorRepository;

@Service
public class AccountingService {
    private static final String CASH = "1000";
    private static final String AR = "1100";
    private static final String TAX_RECOVERABLE = "1300";
    private static final String AP = "2000";
    private static final String TAX_PAYABLE = "2100";
    private static final String OWNER_EQUITY = "3000";
    private static final String SALES = "4000";
    private static final String OPERATING_EXPENSE = "5000";

    private final AccountingAccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ShopContextService shopContextService;

    public AccountingService(AccountingAccountRepository accountRepository, JournalEntryRepository journalEntryRepository, ExpenseRepository expenseRepository, UserRepository userRepository, VendorRepository vendorRepository, ShopContextService shopContextService) {
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.shopContextService = shopContextService;
    }

    public List<AccountingAccount> getAccounts() {
        ensureDefaultAccounts();
        return accountRepository.findByActiveTrueOrderByCodeAsc().stream()
                .filter(account -> account.isSystemAccount()
                        || shopContextService.canAccessTenantShop(account.getShop()))
                .toList();
    }

    public AccountingAccount createAccount(AccountingAccount account) {
        Long shopId = shopContextService.getCurrentTenantShopId().orElse(null);
        boolean duplicateCode = shopId == null
                ? accountRepository.existsByCode(account.getCode())
                : accountRepository.existsByCodeAndShop_Id(account.getCode(), shopId);
        if (duplicateCode) {
            throw new RuntimeException("Account code already exists");
        }
        User admin = currentAdmin();
        applyAdmin(account, admin);
        return accountRepository.save(account);
    }

    public List<Vendor> getVendors() {
        return visibleVendors();
    }

    public Vendor createVendor(Vendor vendor) {
        User admin = currentAdmin();
        normalizeVendor(vendor);
        applyAdmin(vendor, admin);
        return vendorRepository.save(vendor);
    }

    public List<Expense> getExpenses() {
        return visibleRecentExpenses();
    }

    @Transactional
    public Expense createExpense(Expense expense) {
        ensureDefaultAccounts();
        normalizeExpense(expense);
        User admin = currentAdmin();
        applyAdmin(expense, admin);
        Expense savedExpense = expenseRepository.save(expense);
        recordExpense(savedExpense);
        return savedExpense;
    }

    @Transactional
    public Expense recordPayrollCost(PayrollRecord record) {
        ensureDefaultAccounts();
        if (record == null || record.getId() == null) {
            throw new IllegalArgumentException("Payroll record is required");
        }

        User employee = record.getEmployee();
        PayrollPeriod period = record.getPayrollPeriod();
        String employeeName = employee == null ? "Employee" : employee.getFullName();
        BigDecimal payrollCost = amount(record.getGrossPay());

        Expense expense = new Expense();
        expense.setVendor("Payroll - " + employeeName);
        expense.setCategoryKey(ExpenseCategory.PAYROLL);
        expense.setCategory("Payroll");
        expense.setExpenseDate(record.getPaidAt() == null ? LocalDate.now() : record.getPaidAt().toLocalDate());
        expense.setSubtotal(payrollCost);
        expense.setTaxAmount(BigDecimal.ZERO);
        expense.setTotal(payrollCost);
        expense.setStatus("PAID");
        expense.setPaymentMethod("Payroll");
        expense.setNotes("Payroll record #" + record.getId()
                + (period == null ? "" : " / " + period.getStartDate() + " to " + period.getEndDate())
                + (record.getNotes() == null || record.getNotes().isBlank() ? "" : " / " + record.getNotes()));
        expense.setShop(resolvePayrollShop(record));

        User currentUser = currentUser();
        if (currentUser != null) {
            expense.setAdminUserId(currentUser.getId());
            expense.setCreatedBy(currentUser.getEmail());
            if (expense.getShop() == null) {
                expense.setShop(currentUser.getShop());
            }
        }

        Expense savedExpense = expenseRepository.save(expense);
        recordExpense(savedExpense);
        return savedExpense;
    }

    @Transactional
    public Expense payExpense(Long id, PayExpenseRequest request) {
        ensureDefaultAccounts();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        ensureExpenseAccess(expense);

        if ("PAID".equalsIgnoreCase(expense.getStatus())) {
            return expense;
        }

        AccountingPaymentMethod methodKey = request.getPaymentMethodKey() == null ? AccountingPaymentMethod.CASH : request.getPaymentMethodKey();
        expense.setPaymentMethodKey(methodKey);
        expense.setCustomPaymentMethod(request.getCustomPaymentMethod());
        expense.setPaymentMethod(resolvePaymentMethod(methodKey, request.getCustomPaymentMethod()));
        expense.setStatus("PAID");
        expense.setPaidAt(LocalDateTime.now());
        applyAdmin(expense, currentAdmin());
        Expense savedExpense = expenseRepository.save(expense);

        recordExpensePayment(savedExpense);
        return savedExpense;
    }

    @Transactional
    public void recordInvoiceIssued(Invoice invoice) {
        ensureDefaultAccounts();
        if (invoice.getId() == null || journalEntryRepository.existsByReferenceTypeAndReferenceIdAndSource("Invoice", invoice.getId(), "INVOICE_ISSUED")) {
            return;
        }

        JournalEntry entry = new JournalEntry();
        entry.setEntryDate(invoice.getCreatedAt() == null ? LocalDate.now() : invoice.getCreatedAt().toLocalDate());
        entry.setDescription("Invoice #" + invoice.getId() + " issued to " + invoice.getCustomerName());
        entry.setReferenceType("Invoice");
        entry.setReferenceId(invoice.getId());
        entry.setSource("INVOICE_ISSUED");
        applyAdmin(entry, currentAdmin());
        entry.setShop(invoice.getShop());
        BigDecimal total = amount(invoice.getTotal());
        BigDecimal taxAmount = amount(invoice.getTaxAmount());
        BigDecimal revenue = amount(invoice.getSubtotal());
        if (revenue.add(taxAmount).compareTo(total) != 0) {
            revenue = total.subtract(taxAmount).max(BigDecimal.ZERO);
        }

        addDebit(entry, AR, total, "Customer receivable");
        addCredit(entry, SALES, revenue, "Sales revenue");
        if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            addCredit(entry, TAX_PAYABLE, taxAmount, "Sales tax payable");
        }
        saveBalanced(entry);
    }

    @Transactional
    public void recordInvoicePayment(Invoice invoice) {
        BigDecimal paidAmount = amount(invoice.getAmountPaid()).compareTo(BigDecimal.ZERO) > 0
                ? amount(invoice.getAmountPaid())
                : amount(invoice.getTotal());
        recordInvoicePayment(invoice, paidAmount);
    }

    @Transactional
    public void recordInvoicePayment(Invoice invoice, BigDecimal paymentAmount) {
        ensureDefaultAccounts();
        BigDecimal paidAmount = amount(paymentAmount);
        if (invoice.getId() == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        long existingPayments = journalEntryRepository.countByReferenceTypeAndReferenceIdAndSourceStartingWith(
                "Invoice",
                invoice.getId(),
                "INVOICE_PAYMENT");
        String paymentSource = "INVOICE_PAYMENT_" + (existingPayments + 1);
        JournalEntry entry = new JournalEntry();
        entry.setEntryDate(LocalDate.now());
        entry.setDescription("Payment received for invoice #" + invoice.getId() + " - " + paidAmount);
        entry.setReferenceType("Invoice");
        entry.setReferenceId(invoice.getId());
        entry.setSource(paymentSource);
        applyAdmin(entry, currentAdmin());
        entry.setShop(invoice.getShop());
        addDebit(entry, CASH, paidAmount, invoice.getPaymentMethod() == null ? "Payment received" : invoice.getPaymentMethod());
        addCredit(entry, AR, paidAmount, "Receivable cleared");
        saveBalanced(entry);
    }

    public AccountingReport getReport(LocalDate start, LocalDate end) {
        ensureDefaultAccounts();
        LocalDate rangeStart = start == null ? LocalDate.now().withDayOfMonth(1) : start;
        LocalDate rangeEnd = end == null ? LocalDate.now() : end;
        List<JournalEntry> entries = visibleJournalEntriesInRange(rangeStart, rangeEnd);
        List<AccountBalance> balances = buildBalances(entries);
        List<AccountBalance> profitAndLoss = balances.stream()
                .filter(balance -> balance.getType() == AccountType.REVENUE || balance.getType() == AccountType.EXPENSE)
                .toList();
        List<AccountBalance> balanceSheet = balances.stream()
                .filter(balance -> balance.getType() == AccountType.ASSET || balance.getType() == AccountType.LIABILITY || balance.getType() == AccountType.EQUITY)
                .toList();
        BigDecimal revenue = sumType(balances, AccountType.REVENUE);
        BigDecimal expenses = sumType(balances, AccountType.EXPENSE);
        BigDecimal assets = sumType(balances, AccountType.ASSET);
        BigDecimal liabilities = sumType(balances, AccountType.LIABILITY);
        BigDecimal equity = sumType(balances, AccountType.EQUITY).add(revenue.subtract(expenses));

        return new AccountingReport(
                balances,
                profitAndLoss,
                balanceSheet,
                visibleRecentExpenses(),
                entries.stream().limit(25).toList(),
                visibleVendors(),
                revenue,
                expenses,
                revenue.subtract(expenses),
                assets,
                liabilities,
                equity
        );
    }

    public List<DuplicatePaymentDiagnosticResponse> findDuplicateInvoicePaymentEntries() {
        Map<Long, List<JournalEntry>> paymentsByInvoice = journalEntryRepository.findAll().stream()
                .filter(entry -> "Invoice".equalsIgnoreCase(entry.getReferenceType()))
                .filter(entry -> entry.getReferenceId() != null)
                .filter(entry -> entry.getSource() != null && entry.getSource().startsWith("INVOICE_PAYMENT"))
                .filter(entry -> shopContextService.canAccessTenantShop(entry.getShop()))
                .collect(Collectors.groupingBy(
                        JournalEntry::getReferenceId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return paymentsByInvoice.entrySet().stream()
                .map(entry -> {
                    List<JournalEntry> duplicateEntries = entry.getValue().stream()
                            .collect(Collectors.groupingBy(
                                    JournalEntry::getSource,
                                    LinkedHashMap::new,
                                    Collectors.toList()))
                            .values()
                            .stream()
                            .filter(journalEntries -> journalEntries.size() > 1)
                            .flatMap(List::stream)
                            .toList();

                    if (duplicateEntries.isEmpty()) {
                        return null;
                    }

                    List<JournalEntry> journalEntries = duplicateEntries.stream()
                            .sorted(Comparator.comparing(JournalEntry::getEntryDate).thenComparing(JournalEntry::getId))
                            .toList();
                    JournalEntry first = journalEntries.get(0);
                    Shop shop = first.getShop();
                    return new DuplicatePaymentDiagnosticResponse(
                            entry.getKey(),
                            shop == null ? null : shop.getId(),
                            shop == null ? null : shop.getName(),
                            journalEntries.size(),
                            journalEntries.stream().map(JournalEntry::getId).toList(),
                            journalEntries.stream().map(JournalEntry::getSource).toList());
                })
                .filter(response -> response != null)
                .toList();
    }

    private synchronized void ensureDefaultAccounts() {
        seed(CASH, "Cash / Bank", AccountType.ASSET);
        seed(AR, "Accounts Receivable", AccountType.ASSET);
        seed(TAX_RECOVERABLE, "Tax Recoverable", AccountType.ASSET);
        seed(AP, "Accounts Payable", AccountType.LIABILITY);
        seed(TAX_PAYABLE, "Tax Payable", AccountType.LIABILITY);
        seed(OWNER_EQUITY, "Owner Equity", AccountType.EQUITY);
        seed(SALES, "Sales Revenue", AccountType.REVENUE);
        seed(OPERATING_EXPENSE, "Operating Expenses", AccountType.EXPENSE);
    }

    private void seed(String code, String name, AccountType type) {
        accountRepository.findByCode(code).orElseGet(() -> {
            AccountingAccount account = new AccountingAccount();
            account.setCode(code);
            account.setName(name);
            account.setType(type);
            account.setSystemAccount(true);
            applyAdmin(account, fallbackAdmin());
            return accountRepository.save(account);
        });
    }

    private void recordExpense(Expense expense) {
        JournalEntry entry = new JournalEntry();
        entry.setEntryDate(expense.getExpenseDate() == null ? LocalDate.now() : expense.getExpenseDate());
        entry.setDescription("Expense #" + expense.getId() + " - " + expense.getVendor());
        entry.setReferenceType("Expense");
        entry.setReferenceId(expense.getId());
        entry.setSource("EXPENSE");
        entry.setAdminUserId(expense.getAdminUserId());
        entry.setPostedBy(expense.getCreatedBy());
        entry.setShop(expense.getShop());
        addDebit(entry, expenseAccountCode(expense), amount(expense.getSubtotal()), expense.getCategory());
        if (amount(expense.getTaxAmount()).compareTo(BigDecimal.ZERO) > 0) {
            addDebit(entry, TAX_RECOVERABLE, amount(expense.getTaxAmount()), "Recoverable tax");
        }
        addCredit(entry, "PAID".equalsIgnoreCase(expense.getStatus()) ? CASH : AP, amount(expense.getTotal()), expense.getPaymentMethod());
        saveBalanced(entry);
    }

    private void recordExpensePayment(Expense expense) {
        if (expense.getId() == null || journalEntryRepository.existsByReferenceTypeAndReferenceIdAndSource("Expense", expense.getId(), "EXPENSE_PAYMENT")) {
            return;
        }

        JournalEntry entry = new JournalEntry();
        entry.setEntryDate(LocalDate.now());
        entry.setDescription("Payment made for expense #" + expense.getId() + " - " + expense.getVendor());
        entry.setReferenceType("Expense");
        entry.setReferenceId(expense.getId());
        entry.setSource("EXPENSE_PAYMENT");
        entry.setAdminUserId(expense.getAdminUserId());
        entry.setPostedBy(expense.getCreatedBy());
        entry.setShop(expense.getShop());
        addDebit(entry, AP, amount(expense.getTotal()), "Payable cleared");
        addCredit(entry, CASH, amount(expense.getTotal()), expense.getPaymentMethod());
        saveBalanced(entry);
    }

    private void normalizeExpense(Expense expense) {
        BigDecimal subtotal = amount(expense.getSubtotal());
        BigDecimal taxAmount = amount(expense.getTaxAmount());
        BigDecimal total = amount(expense.getTotal());
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            total = subtotal.add(taxAmount);
        }
        expense.setSubtotal(subtotal);
        expense.setTaxAmount(taxAmount);
        expense.setTotal(total);
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
        }
        if (expense.getStatus() == null || expense.getStatus().isBlank()) {
            expense.setStatus("PAID");
        }
        if ("UNPAID".equalsIgnoreCase(expense.getStatus()) && expense.getDueDate() == null) {
            expense.setDueDate((expense.getExpenseDate() == null ? LocalDate.now() : expense.getExpenseDate()).plusDays(14));
        }
        if (expense.getCategoryKey() == null) {
            expense.setCategoryKey(ExpenseCategory.SUPPLIES);
        }
        expense.setCategory(resolveExpenseCategory(expense.getCategoryKey(), expense.getCustomCategory()));
        if (expense.getPaymentMethodKey() == null) {
            expense.setPaymentMethodKey(AccountingPaymentMethod.CASH);
        }
        expense.setPaymentMethod(resolvePaymentMethod(expense.getPaymentMethodKey(), expense.getCustomPaymentMethod()));
        if (expense.getVendorId() != null) {
            vendorRepository.findById(expense.getVendorId()).ifPresent(vendor -> {
                ensureVendorAccess(vendor);
                expense.setVendor(vendor.getName());
                if (expense.getCustomCategory() == null || expense.getCustomCategory().isBlank()) {
                    expense.setCategoryKey(expense.getCategoryKey() == null ? ExpenseCategory.SUPPLIES : expense.getCategoryKey());
                }
            });
        }
    }

    private void normalizeVendor(Vendor vendor) {
        if (vendor.getCategoryKey() == null) {
            vendor.setCategoryKey(VendorCategory.GENERAL);
        }
        vendor.setCategory(resolveVendorCategory(vendor.getCategoryKey(), vendor.getCustomCategory()));
    }

    private List<AccountBalance> buildBalances(List<JournalEntry> entries) {
        Map<Long, RunningBalance> running = new LinkedHashMap<>();
        for (AccountingAccount account : getAccounts()) {
            running.put(account.getId(), new RunningBalance(account));
        }

        for (JournalEntry entry : entries) {
            for (JournalEntryLine line : entry.getLines()) {
                running.computeIfAbsent(line.getAccount().getId(), id -> new RunningBalance(line.getAccount())).add(line.getDebit(), line.getCredit());
            }
        }

        return running.values().stream()
                .map(RunningBalance::toAccountBalance)
                .sorted(Comparator.comparing(AccountBalance::getCode))
                .toList();
    }

    private BigDecimal sumType(List<AccountBalance> balances, AccountType type) {
        return balances.stream()
                .filter(balance -> balance.getType() == type)
                .map(AccountBalance::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void addDebit(JournalEntry entry, String accountCode, BigDecimal amount, String memo) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account(accountCode));
        line.setDebit(amount);
        line.setMemo(memo);
        entry.addLine(line);
    }

    private void addCredit(JournalEntry entry, String accountCode, BigDecimal amount, String memo) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account(accountCode));
        line.setCredit(amount);
        line.setMemo(memo);
        entry.addLine(line);
    }

    private AccountingAccount account(String code) {
        Long shopId = shopContextService.getCurrentTenantShopId().orElse(null);
        if (shopId != null) {
            return accountRepository.findByCodeAndShop_Id(code, shopId)
                    .or(() -> accountRepository.findByCode(code).filter(AccountingAccount::isSystemAccount))
                    .orElseThrow(() -> new RuntimeException("Accounting account " + code + " is missing"));
        }

        return accountRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Accounting account " + code + " is missing"));
    }

    private String expenseAccountCode(Expense expense) {
        if (expense.getExpenseAccountId() == null) {
            return OPERATING_EXPENSE;
        }

        AccountingAccount account = accountRepository.findById(expense.getExpenseAccountId())
                .orElseThrow(() -> new RuntimeException("Expense account not found"));

        if (account.getType() != AccountType.EXPENSE) {
            throw new RuntimeException("Selected account must be an EXPENSE account");
        }

        ensureAccountAccess(account);
        return account.getCode();
    }

    private void saveBalanced(JournalEntry entry) {
        BigDecimal debits = entry.getTotalDebits();
        BigDecimal credits = entry.getTotalCredits();
        if (debits.compareTo(credits) != 0) {
            throw new RuntimeException("Journal entry is not balanced");
        }
        if (!entry.getLines().isEmpty()) {
            journalEntryRepository.save(entry);
        }
    }

    private BigDecimal amount(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveExpenseCategory(ExpenseCategory category, String customCategory) {
        if (category == ExpenseCategory.OTHER && customCategory != null && !customCategory.isBlank()) {
            return customCategory.trim();
        }
        return label(category.name());
    }

    private String resolveVendorCategory(VendorCategory category, String customCategory) {
        if (category == VendorCategory.OTHER && customCategory != null && !customCategory.isBlank()) {
            return customCategory.trim();
        }
        return label(category.name());
    }

    private String resolvePaymentMethod(AccountingPaymentMethod method, String customPaymentMethod) {
        if (method == AccountingPaymentMethod.OTHER && customPaymentMethod != null && !customPaymentMethod.isBlank()) {
            return customPaymentMethod.trim();
        }
        return label(method.name());
    }

    private String label(String key) {
        String[] parts = key.toLowerCase().split("_");
        List<String> words = new ArrayList<>();
        for (String part : parts) {
            words.add(part.substring(0, 1).toUpperCase() + part.substring(1));
        }
        return String.join(" ", words);
    }

    private User currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EMPLOYEE) {
                    shopContextService.requireShopForAdminOrEmployee();
                }
                return user;
            }
        }
        return fallbackAdmin();
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            return userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return fallbackAdmin();
    }

    private Shop resolvePayrollShop(PayrollRecord record) {
        if (record.getPayrollPeriod() != null && record.getPayrollPeriod().getShop() != null) {
            return record.getPayrollPeriod().getShop();
        }

        if (record.getEmployee() != null) {
            return record.getEmployee().getShop();
        }

        return null;
    }

    private User fallbackAdmin() {
        List<User> admins = userRepository.findByRoleOrderByCreatedAtDesc(UserRole.ADMIN);
        return admins.isEmpty() ? null : admins.get(0);
    }

    private void applyAdmin(AccountingAccount account, User admin) {
        if (admin == null) {
            return;
        }
        account.setAdminUserId(admin.getId());
        account.setCreatedBy(admin.getEmail());
        if (!account.isSystemAccount()) {
            account.setShop(admin.getShop());
        }
    }

    private void applyAdmin(Expense expense, User admin) {
        if (admin == null) {
            return;
        }
        expense.setAdminUserId(admin.getId());
        expense.setCreatedBy(admin.getEmail());
        expense.setShop(admin.getShop());
    }

    private void applyAdmin(JournalEntry entry, User admin) {
        if (admin == null) {
            return;
        }
        entry.setAdminUserId(admin.getId());
        entry.setPostedBy(admin.getEmail());
        entry.setShop(admin.getShop());
    }

    private void applyAdmin(Vendor vendor, User admin) {
        if (admin == null) {
            return;
        }
        vendor.setAdminUserId(admin.getId());
        vendor.setCreatedBy(admin.getEmail());
        vendor.setShop(admin.getShop());
    }

    private void ensureExpenseAccess(Expense expense) {
        if (!shopContextService.canAccessTenantShop(expense.getShop())) {
            throw new AccessDeniedException("You do not have permission to access this expense.");
        }
    }

    private void ensureVendorAccess(Vendor vendor) {
        if (!shopContextService.canAccessTenantShop(vendor.getShop())) {
            throw new AccessDeniedException("You do not have permission to access this vendor.");
        }
    }

    private void ensureAccountAccess(AccountingAccount account) {
        if (!account.isSystemAccount() && !shopContextService.canAccessTenantShop(account.getShop())) {
            throw new AccessDeniedException("You do not have permission to access this accounting account.");
        }
    }

    private List<Expense> visibleRecentExpenses() {
        return expenseRepository.findAll().stream()
                .filter(expense -> shopContextService.canAccessTenantShop(expense.getShop()))
                .sorted(Comparator
                        .comparing((Expense expense) -> expense.getExpenseDate() == null ? LocalDate.MIN : expense.getExpenseDate())
                        .reversed()
                        .thenComparing(expense -> expense.getId() == null ? 0L : expense.getId(), Comparator.reverseOrder()))
                .limit(25)
                .toList();
    }

    private List<Vendor> visibleVendors() {
        return vendorRepository.findAll().stream()
                .filter(vendor -> shopContextService.canAccessTenantShop(vendor.getShop()))
                .sorted(Comparator.comparing(
                        vendor -> vendor.getName() == null ? "" : vendor.getName(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<JournalEntry> visibleJournalEntriesInRange(LocalDate start, LocalDate end) {
        return journalEntryRepository.findAll().stream()
                .filter(entry -> shopContextService.canAccessTenantShop(entry.getShop()))
                .filter(entry -> entry.getEntryDate() != null)
                .filter(entry -> !entry.getEntryDate().isBefore(start) && !entry.getEntryDate().isAfter(end))
                .sorted(Comparator
                        .comparing(JournalEntry::getEntryDate)
                        .reversed()
                        .thenComparing(entry -> entry.getId() == null ? 0L : entry.getId(), Comparator.reverseOrder()))
                .toList();
    }

    private static class RunningBalance {
        private final AccountingAccount account;
        private BigDecimal debits = BigDecimal.ZERO;
        private BigDecimal credits = BigDecimal.ZERO;

        RunningBalance(AccountingAccount account) {
            this.account = account;
        }

        void add(BigDecimal debit, BigDecimal credit) {
            debits = debits.add(debit == null ? BigDecimal.ZERO : debit);
            credits = credits.add(credit == null ? BigDecimal.ZERO : credit);
        }

        AccountBalance toAccountBalance() {
            BigDecimal balance = switch (account.getType()) {
                case ASSET, EXPENSE -> debits.subtract(credits);
                case LIABILITY, EQUITY, REVENUE -> credits.subtract(debits);
            };
            return new AccountBalance(account.getId(), account.getCode(), account.getName(), account.getType(), debits, credits, balance);
        }
    }
}
