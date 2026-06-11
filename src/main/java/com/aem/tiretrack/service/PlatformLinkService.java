package com.aem.tiretrack.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.PlatformLinkRecordResponse;
import com.aem.tiretrack.dto.PlatformLinkRequest;
import com.aem.tiretrack.dto.ShopLocationResponse;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.AccountingAccount;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.AuditLog;
import com.aem.tiretrack.model.EmployeeAttendance;
import com.aem.tiretrack.model.EmployeeLoan;
import com.aem.tiretrack.model.Estimate;
import com.aem.tiretrack.model.Expense;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.JournalEntry;
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollRecord;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.Vendor;
import com.aem.tiretrack.model.WorkOrder;
import com.aem.tiretrack.repository.AccountingAccountRepository;
import com.aem.tiretrack.repository.AppNotificationRepository;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.AuditLogRepository;
import com.aem.tiretrack.repository.EmployeeAttendanceRepository;
import com.aem.tiretrack.repository.EmployeeLoanRepository;
import com.aem.tiretrack.repository.EstimateRepository;
import com.aem.tiretrack.repository.ExpenseRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.JournalEntryRepository;
import com.aem.tiretrack.repository.PayrollPeriodRepository;
import com.aem.tiretrack.repository.PayrollRecordRepository;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.repository.VendorRepository;
import com.aem.tiretrack.repository.WorkOrderRepository;

@Service
public class PlatformLinkService {
    private final UserRepository userRepository;
    private final TireRepository tireRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final EstimateRepository estimateRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final VendorRepository vendorRepository;
    private final ExpenseRepository expenseRepository;
    private final AccountingAccountRepository accountingAccountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmployeeAttendanceRepository employeeAttendanceRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final ShopRepository shopRepository;
    private final ShopLocationRepository shopLocationRepository;

    public PlatformLinkService(
            UserRepository userRepository,
            TireRepository tireRepository,
            AppointmentRepository appointmentRepository,
            InvoiceRepository invoiceRepository,
            EstimateRepository estimateRepository,
            WorkOrderRepository workOrderRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollRecordRepository payrollRecordRepository,
            VendorRepository vendorRepository,
            ExpenseRepository expenseRepository,
            AccountingAccountRepository accountingAccountRepository,
            JournalEntryRepository journalEntryRepository,
            AppNotificationRepository appNotificationRepository,
            AuditLogRepository auditLogRepository,
            EmployeeAttendanceRepository employeeAttendanceRepository,
            EmployeeLoanRepository employeeLoanRepository,
            ShopRepository shopRepository,
            ShopLocationRepository shopLocationRepository) {
        this.userRepository = userRepository;
        this.tireRepository = tireRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.estimateRepository = estimateRepository;
        this.workOrderRepository = workOrderRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollRecordRepository = payrollRecordRepository;
        this.vendorRepository = vendorRepository;
        this.expenseRepository = expenseRepository;
        this.accountingAccountRepository = accountingAccountRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.appNotificationRepository = appNotificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.employeeAttendanceRepository = employeeAttendanceRepository;
        this.employeeLoanRepository = employeeLoanRepository;
        this.shopRepository = shopRepository;
        this.shopLocationRepository = shopLocationRepository;
    }

    @Transactional(readOnly = true)
    public List<PlatformLinkRecordResponse> getAllLinkableRecords() {
        List<PlatformLinkRecordResponse> records = new ArrayList<>();

        userRepository.findAll().forEach(user -> records.add(toRecord(user)));
        tireRepository.findAll().forEach(tire -> records.add(toRecord(tire)));
        appointmentRepository.findAll().forEach(appointment -> records.add(toRecord(appointment)));
        invoiceRepository.findAll().forEach(invoice -> records.add(toRecord(invoice)));
        estimateRepository.findAll().forEach(estimate -> records.add(toRecord(estimate)));
        workOrderRepository.findAll().forEach(workOrder -> records.add(toRecord(workOrder)));
        payrollPeriodRepository.findAll().forEach(period -> records.add(toRecord(period)));
        payrollRecordRepository.findAll().forEach(record -> records.add(toRecord(record)));
        vendorRepository.findAll().forEach(vendor -> records.add(toRecord(vendor)));
        expenseRepository.findAll().forEach(expense -> records.add(toRecord(expense)));
        accountingAccountRepository.findAll().forEach(account -> records.add(toRecord(account)));
        journalEntryRepository.findAll().forEach(entry -> records.add(toRecord(entry)));
        appNotificationRepository.findAll().forEach(notification -> records.add(toRecord(notification)));
        auditLogRepository.findAll().forEach(log -> records.add(toRecord(log)));
        employeeAttendanceRepository.findAll().forEach(attendance -> records.add(toRecord(attendance)));
        employeeLoanRepository.findAll().forEach(loan -> records.add(toRecord(loan)));

        return records.stream()
                .sorted(Comparator
                        .comparing(PlatformLinkRecordResponse::getType)
                        .thenComparing(PlatformLinkRecordResponse::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShopLocationResponse> getAllLocations() {
        return shopLocationRepository.findAll().stream()
                .map(ShopLocationResponse::new)
                .toList();
    }

    @Transactional
    public PlatformLinkRecordResponse assignRecord(String recordType, Long recordId, PlatformLinkRequest request) {
        String type = normalizeType(recordType);

        if ("USER".equals(type)) {
            User user = userRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + recordId));
            ShopAssignment assignment = resolveShopAssignment(request.getShopId(), request.getLocationId());
            if (user.getRole() == UserRole.SUPER_ADMIN && assignment.shop() != null) {
                throw new IllegalArgumentException("SUPER_ADMIN accounts should stay platform-level.");
            }
            if (user.getRole() == UserRole.OWNER && assignment.location() != null) {
                throw new IllegalArgumentException("OWNER users belong to the shop, not a single location.");
            }
            if (assignment.location() != null
                    && user.getRole() != UserRole.ADMIN
                    && user.getRole() != UserRole.EMPLOYEE) {
                throw new IllegalArgumentException("Only ADMIN or EMPLOYEE users can be assigned to a location.");
            }
            user.setShop(assignment.shop());
            user.setShopLocation(assignment.location());
            return toRecord(userRepository.save(user));
        }

        if ("TIRE".equals(type)) {
            Tire tire = tireRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Tire not found with id: " + recordId));
            ShopAssignment assignment = resolveShopAssignment(request.getShopId(), request.getLocationId());
            tire.setShop(assignment.shop());
            tire.setShopLocation(assignment.location());
            return toRecord(tireRepository.save(tire));
        }

        ShopAssignment assignment = resolveShopAssignment(request.getShopId(), request.getLocationId());
        Shop shop = assignment.shop();
        ShopLocation location = assignment.location();

        if ("APPOINTMENT".equals(type)) {
            Appointment appointment = appointmentRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + recordId));
            appointment.setShop(shop);
            appointment.setShopLocation(location);
            return toRecord(appointmentRepository.save(appointment));
        }

        if ("INVOICE".equals(type)) {
            Invoice invoice = invoiceRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + recordId));
            invoice.setShop(shop);
            invoice.setShopLocation(location);
            return toRecord(invoiceRepository.save(invoice));
        }

        if ("ESTIMATE".equals(type)) {
            Estimate estimate = estimateRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Estimate not found with id: " + recordId));
            estimate.setShop(shop);
            estimate.setShopLocation(location);
            return toRecord(estimateRepository.save(estimate));
        }

        if ("WORK_ORDER".equals(type)) {
            WorkOrder workOrder = workOrderRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Work order not found with id: " + recordId));
            workOrder.setShop(shop);
            workOrder.setShopLocation(location);
            return toRecord(workOrderRepository.save(workOrder));
        }

        if ("PAYROLL_PERIOD".equals(type)) {
            PayrollPeriod period = payrollPeriodRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Payroll period not found with id: " + recordId));
            period.setShop(shop);
            period.setShopLocation(location);
            return toRecord(payrollPeriodRepository.save(period));
        }

        if ("PAYROLL_RECORD".equals(type)) {
            PayrollRecord record = payrollRecordRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Payroll record not found with id: " + recordId));
            if (record.getEmployee() != null && record.getEmployee().getShop() == null) {
                record.getEmployee().setShop(shop);
            }
            if (record.getPayrollPeriod() != null && record.getPayrollPeriod().getShop() == null) {
                record.getPayrollPeriod().setShop(shop);
                record.getPayrollPeriod().setShopLocation(location);
            }
            return toRecord(payrollRecordRepository.save(record));
        }

        if ("VENDOR".equals(type)) {
            Vendor vendor = vendorRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Vendor not found with id: " + recordId));
            vendor.setShop(shop);
            return toRecord(vendorRepository.save(vendor));
        }

        if ("EXPENSE".equals(type)) {
            Expense expense = expenseRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Expense not found with id: " + recordId));
            expense.setShop(shop);
            return toRecord(expenseRepository.save(expense));
        }

        if ("ACCOUNTING_ACCOUNT".equals(type)) {
            AccountingAccount account = accountingAccountRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Accounting account not found with id: " + recordId));
            account.setShop(shop);
            return toRecord(accountingAccountRepository.save(account));
        }

        if ("JOURNAL_ENTRY".equals(type)) {
            JournalEntry entry = journalEntryRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Journal entry not found with id: " + recordId));
            entry.setShop(shop);
            return toRecord(journalEntryRepository.save(entry));
        }

        if ("APP_NOTIFICATION".equals(type)) {
            AppNotification notification = appNotificationRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("App notification not found with id: " + recordId));
            notification.setShop(shop);
            return toRecord(appNotificationRepository.save(notification));
        }

        if ("AUDIT_LOG".equals(type)) {
            AuditLog log = auditLogRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Audit log not found with id: " + recordId));
            log.setShop(shop);
            return toRecord(auditLogRepository.save(log));
        }

        if ("EMPLOYEE_ATTENDANCE".equals(type)) {
            EmployeeAttendance attendance = employeeAttendanceRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with id: " + recordId));
            attendance.setShop(shop);
            attendance.setShopLocation(location);
            return toRecord(employeeAttendanceRepository.save(attendance));
        }

        if ("EMPLOYEE_LOAN".equals(type)) {
            EmployeeLoan loan = employeeLoanRepository.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee loan not found with id: " + recordId));
            loan.setShop(shop);
            return toRecord(employeeLoanRepository.save(loan));
        }

        throw new IllegalArgumentException("Unsupported platform link type: " + recordType);
    }

    @Transactional
    public void transferLegacySingleShopData(Shop shop) {
        if (shop == null) {
            return;
        }

        // First-tenant backfill: old TireTrack records were created before shop_id existed.
        // Only unassigned records are stamped, so existing tenant-linked records are never moved.
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getShop() == null)
                .filter(user -> user.getRole() != UserRole.SUPER_ADMIN)
                .toList();
        users.forEach(user -> user.setShop(shop));
        userRepository.saveAll(users);

        List<Tire> tires = tireRepository.findAll().stream()
                .filter(tire -> tire.getShop() == null)
                .toList();
        tires.forEach(tire -> tire.setShop(shop));
        tireRepository.saveAll(tires);

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getShop() == null)
                .toList();
        appointments.forEach(appointment -> appointment.setShop(shop));
        appointmentRepository.saveAll(appointments);

        List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getShop() == null)
                .toList();
        invoices.forEach(invoice -> invoice.setShop(shop));
        invoiceRepository.saveAll(invoices);

        List<Estimate> estimates = estimateRepository.findAll().stream()
                .filter(estimate -> estimate.getShop() == null)
                .toList();
        estimates.forEach(estimate -> estimate.setShop(shop));
        estimateRepository.saveAll(estimates);

        List<WorkOrder> workOrders = workOrderRepository.findAll().stream()
                .filter(workOrder -> workOrder.getShop() == null)
                .toList();
        workOrders.forEach(workOrder -> workOrder.setShop(shop));
        workOrderRepository.saveAll(workOrders);

        List<PayrollPeriod> periods = payrollPeriodRepository.findAll().stream()
                .filter(period -> period.getShop() == null)
                .toList();
        periods.forEach(period -> period.setShop(shop));
        payrollPeriodRepository.saveAll(periods);

        List<PayrollRecord> records = payrollRecordRepository.findAll();
        records.forEach(record -> {
            if (record.getEmployee() != null && record.getEmployee().getShop() == null) {
                record.getEmployee().setShop(shop);
            }
            if (record.getPayrollPeriod() != null && record.getPayrollPeriod().getShop() == null) {
                record.getPayrollPeriod().setShop(shop);
            }
        });
        payrollRecordRepository.saveAll(records);

        List<Vendor> vendors = vendorRepository.findAll().stream()
                .filter(vendor -> vendor.getShop() == null)
                .toList();
        vendors.forEach(vendor -> vendor.setShop(shop));
        vendorRepository.saveAll(vendors);

        List<Expense> expenses = expenseRepository.findAll().stream()
                .filter(expense -> expense.getShop() == null)
                .toList();
        expenses.forEach(expense -> expense.setShop(shop));
        expenseRepository.saveAll(expenses);

        List<AccountingAccount> accounts = accountingAccountRepository.findAll().stream()
                .filter(account -> account.getShop() == null)
                .filter(account -> !account.isSystemAccount())
                .toList();
        accounts.forEach(account -> account.setShop(shop));
        accountingAccountRepository.saveAll(accounts);

        List<JournalEntry> entries = journalEntryRepository.findAll().stream()
                .filter(entry -> entry.getShop() == null)
                .toList();
        entries.forEach(entry -> entry.setShop(shop));
        journalEntryRepository.saveAll(entries);

        List<AppNotification> notifications = appNotificationRepository.findAll().stream()
                .filter(notification -> notification.getShop() == null)
                .toList();
        notifications.forEach(notification -> notification.setShop(shop));
        appNotificationRepository.saveAll(notifications);

        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log -> log.getShop() == null)
                .toList();
        logs.forEach(log -> log.setShop(shop));
        auditLogRepository.saveAll(logs);

        List<EmployeeAttendance> attendances = employeeAttendanceRepository.findAll().stream()
                .filter(attendance -> attendance.getShop() == null)
                .toList();
        attendances.forEach(attendance -> attendance.setShop(resolveLegacyShop(attendance.getEmployee(), shop)));
        employeeAttendanceRepository.saveAll(attendances);

        List<EmployeeLoan> loans = employeeLoanRepository.findAll().stream()
                .filter(loan -> loan.getShop() == null)
                .toList();
        loans.forEach(loan -> loan.setShop(resolveLegacyShop(loan.getEmployee(), shop)));
        employeeLoanRepository.saveAll(loans);
    }

    private ShopAssignment resolveShopAssignment(Long shopId, Long locationId) {
        Shop shop = resolveActiveShop(shopId);
        ShopLocation location = null;

        if (locationId != null) {
            location = shopLocationRepository.findById(locationId)
                    .orElseThrow(() -> new IllegalArgumentException("Shop location not found with id: " + locationId));

            if (!location.isActive()) {
                throw new IllegalArgumentException("Cannot assign records to an inactive location.");
            }

            if (!location.getShop().isActive()) {
                throw new IllegalArgumentException("Cannot assign records to an inactive shop.");
            }

            if (shop != null && !location.getShop().getId().equals(shop.getId())) {
                throw new IllegalArgumentException("Location does not belong to the selected shop.");
            }

            shop = location.getShop();
        }

        return new ShopAssignment(shop, location);
    }

    private Shop resolveActiveShop(Long shopId) {
        if (shopId == null) {
            return null;
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));

        if (!shop.isActive()) {
            throw new IllegalArgumentException("Cannot assign records to an inactive shop.");
        }

        return shop;
    }

    private String normalizeType(String recordType) {
        return String.valueOf(recordType == null ? "" : recordType)
                .trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);
    }

    private PlatformLinkRecordResponse toRecord(User user) {
        Shop shop = user.getShop();
        ShopLocation location = user.getShopLocation();
        return new PlatformLinkRecordResponse(
                "USER",
                user.getId(),
                user.getFullName(),
                joinNonBlank(user.getEmail(), user.getPhone()),
                user.getRole() + (user.isActive() ? "" : " / INACTIVE"),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(Tire tire) {
        Shop shop = tire.getShop();
        ShopLocation location = tire.getShopLocation();
        return new PlatformLinkRecordResponse(
                "TIRE",
                tire.getId(),
                joinNonBlank(tire.getBrand(), tire.getModel(), tire.getTireSize()),
                "Qty " + tire.getQuantity() + " / " + joinNonBlank(tire.getLocation(), nameOf(location)),
                String.valueOf(tire.getCondition()),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(Appointment appointment) {
        Shop shop = appointment.getShop();
        ShopLocation location = appointment.getShopLocation();
        return new PlatformLinkRecordResponse(
                "APPOINTMENT",
                appointment.getId(),
                appointment.getCustomerName(),
                joinNonBlank(String.valueOf(appointment.getAppointmentDate()), appointment.getVehicle(), appointment.getPhone()),
                String.valueOf(appointment.getStatus()),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(Invoice invoice) {
        Shop shop = invoice.getShop();
        ShopLocation location = invoice.getShopLocation();
        return new PlatformLinkRecordResponse(
                "INVOICE",
                invoice.getId(),
                "Invoice #" + invoice.getId() + " - " + invoice.getCustomerName(),
                joinNonBlank(String.valueOf(invoice.getTotal()), invoice.getVehicle(), String.valueOf(invoice.getCreatedAt())),
                invoice.getStatus(),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(Estimate estimate) {
        Shop shop = estimate.getShop();
        ShopLocation location = estimate.getShopLocation();
        return new PlatformLinkRecordResponse(
                "ESTIMATE",
                estimate.getId(),
                joinNonBlank(estimate.getEstimateNumber(), estimate.getCustomerName()),
                joinNonBlank(String.valueOf(estimate.getTotal()), estimate.getVehicle(), String.valueOf(estimate.getCreatedAt())),
                String.valueOf(estimate.getStatus()),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(WorkOrder workOrder) {
        Shop shop = workOrder.getShop();
        ShopLocation location = workOrder.getShopLocation();
        return new PlatformLinkRecordResponse(
                "WORK_ORDER",
                workOrder.getId(),
                workOrder.getCustomerName(),
                joinNonBlank(workOrder.getVehicle(), String.valueOf(workOrder.getServiceType()), workOrder.getPhone()),
                String.valueOf(workOrder.getStatus()),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(PayrollPeriod period) {
        Shop shop = period.getShop();
        ShopLocation location = period.getShopLocation();
        return new PlatformLinkRecordResponse(
                "PAYROLL_PERIOD",
                period.getId(),
                period.getStartDate() + " to " + period.getEndDate(),
                period.getNotes(),
                String.valueOf(period.getStatus()),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(PayrollRecord record) {
        Shop shop = record.getPayrollPeriod() == null ? null : record.getPayrollPeriod().getShop();
        ShopLocation location = record.getPayrollPeriod() == null ? null : record.getPayrollPeriod().getShopLocation();
        if (shop == null && record.getEmployee() != null) {
            shop = record.getEmployee().getShop();
        }
        if (location == null && record.getEmployee() != null) {
            location = record.getEmployee().getShopLocation();
        }
        return new PlatformLinkRecordResponse(
                "PAYROLL_RECORD",
                record.getId(),
                record.getEmployee() == null ? "Payroll record #" + record.getId() : record.getEmployee().getFullName(),
                record.getPayrollPeriod() == null ? null : record.getPayrollPeriod().getStartDate() + " to " + record.getPayrollPeriod().getEndDate(),
                String.valueOf(record.getStatus()),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(Vendor vendor) {
        Shop shop = vendor.getShop();
        return new PlatformLinkRecordResponse(
                "VENDOR",
                vendor.getId(),
                vendor.getName(),
                joinNonBlank(vendor.getEmail(), vendor.getPhone(), vendor.getCategory()),
                "VENDOR",
                idOf(shop),
                nameOf(shop),
                null,
                null);
    }

    private PlatformLinkRecordResponse toRecord(Expense expense) {
        Shop shop = expense.getShop();
        return new PlatformLinkRecordResponse(
                "EXPENSE",
                expense.getId(),
                expense.getVendor(),
                joinNonBlank(String.valueOf(expense.getExpenseDate()), String.valueOf(expense.getTotal()), expense.getCategory()),
                expense.getStatus(),
                idOf(shop),
                nameOf(shop),
                null,
                null);
    }

    private PlatformLinkRecordResponse toRecord(AccountingAccount account) {
        Shop shop = account.getShop();
        return new PlatformLinkRecordResponse(
                "ACCOUNTING_ACCOUNT",
                account.getId(),
                account.getCode() + " - " + account.getName(),
                String.valueOf(account.getType()),
                account.isActive() ? "ACTIVE" : "INACTIVE",
                idOf(shop),
                nameOf(shop),
                null,
                null);
    }

    private PlatformLinkRecordResponse toRecord(JournalEntry entry) {
        Shop shop = entry.getShop();
        return new PlatformLinkRecordResponse(
                "JOURNAL_ENTRY",
                entry.getId(),
                entry.getDescription(),
                joinNonBlank(String.valueOf(entry.getEntryDate()), entry.getReferenceType(), String.valueOf(entry.getReferenceId())),
                joinNonBlank(entry.getSource(), entry.getPostedBy()),
                idOf(shop),
                nameOf(shop),
                null,
                null);
    }

    private PlatformLinkRecordResponse toRecord(AppNotification notification) {
        Shop shop = notification.getShop();
        return new PlatformLinkRecordResponse(
                "APP_NOTIFICATION",
                notification.getId(),
                notification.getTitle(),
                joinNonBlank(notification.getMessage(), notification.getTargetTab()),
                String.valueOf(notification.getRecipientRole()),
                idOf(shop),
                nameOf(shop),
                null,
                null);
    }

    private PlatformLinkRecordResponse toRecord(AuditLog log) {
        Shop shop = log.getShop();
        return new PlatformLinkRecordResponse(
                "AUDIT_LOG",
                log.getId(),
                log.getMessage(),
                joinNonBlank(log.getEntityType(), String.valueOf(log.getEntityId()), log.getPerformedBy()),
                log.getAction(),
                idOf(shop),
                nameOf(shop),
                null,
                null);
    }

    private PlatformLinkRecordResponse toRecord(EmployeeAttendance attendance) {
        Shop shop = attendance.getShop();
        ShopLocation location = attendance.getShopLocation();
        return new PlatformLinkRecordResponse(
                "EMPLOYEE_ATTENDANCE",
                attendance.getId(),
                attendance.getEmployeeName(),
                joinNonBlank(String.valueOf(attendance.getWorkDate()), String.valueOf(attendance.getWorkedHours())),
                String.valueOf(attendance.getStatus()),
                idOf(shop),
                nameOf(shop),
                idOf(location),
                nameOf(location));
    }

    private PlatformLinkRecordResponse toRecord(EmployeeLoan loan) {
        Shop shop = loan.getShop();
        return new PlatformLinkRecordResponse(
                "EMPLOYEE_LOAN",
                loan.getId(),
                loan.getEmployeeName(),
                joinNonBlank(String.valueOf(loan.getRemainingBalance()), loan.getNotes()),
                String.valueOf(loan.getStatus()),
                idOf(shop),
                nameOf(shop),
                null,
                null);
    }

    private Shop resolveLegacyShop(User owner, Shop fallbackShop) {
        return owner != null && owner.getShop() != null ? owner.getShop() : fallbackShop;
    }

    private Long idOf(Shop shop) {
        return shop == null ? null : shop.getId();
    }

    private Long idOf(ShopLocation location) {
        return location == null ? null : location.getId();
    }

    private String nameOf(Shop shop) {
        return shop == null ? null : shop.getName();
    }

    private String nameOf(ShopLocation location) {
        return location == null ? null : location.getName();
    }

    private String joinNonBlank(String... values) {
        List<String> parts = new ArrayList<>();

        for (String value : values) {
            if (value != null && !value.isBlank() && !"null".equalsIgnoreCase(value)) {
                parts.add(value);
            }
        }

        return String.join(" / ", parts);
    }

    private record ShopAssignment(Shop shop, ShopLocation location) {}
}
