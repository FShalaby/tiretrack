package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.aem.tiretrack.dto.DashboardLocationBreakdown;
import com.aem.tiretrack.dto.DashboardSummary;
import com.aem.tiretrack.dto.SalesData;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.ExpenseCategory;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.enums.WorkOrderStatus;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Expense;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkOrder;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.ExpenseRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.repository.WorkOrderRepository;

@Service
public class DashboardService 
{
    private final TireRepository tireRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ShopLocationRepository shopLocationRepository;
    private final ShopContextService shopContextService;

    public DashboardService(
            TireRepository tireRepository,
            AppointmentRepository appointmentRepository,
            InvoiceRepository invoiceRepository,
            WorkOrderRepository workOrderRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            ShopLocationRepository shopLocationRepository,
            ShopContextService shopContextService) {
        this.tireRepository = tireRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.shopLocationRepository = shopLocationRepository;
        this.shopContextService = shopContextService;
    }

    public DashboardSummary getDashboardSummary() {
        return getDashboardSummary(null);
    }

    public DashboardSummary getDashboardSummary(Long locationId) {
        LocalDate today = LocalDate.now();
        ShopLocation selectedLocation = shopContextService.resolveAccessibleLocation(locationId, null, false).orElse(null);

        LocalDateTime startOfDay = today.atTime(LocalTime.MIN);
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Tire> visibleTires = tireRepository.findAll().stream()
                .filter(tire -> shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation()))
                .filter(tire -> matchesLocation(tire.getShopLocation(), selectedLocation))
                .toList();
        List<Invoice> visibleInvoices = invoiceRepository.findAll().stream()
                .filter(invoice -> shopContextService.canAccessTenantResource(invoice.getShop(), invoice.getShopLocation()))
                .filter(invoice -> matchesLocation(invoice.getShopLocation(), selectedLocation))
                .toList();
        List<Appointment> visibleAppointments = appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay).stream()
                .filter(appointment -> shopContextService.canAccessTenantResource(appointment.getShop(), appointment.getShopLocation()))
                .filter(appointment -> matchesLocation(appointment.getShopLocation(), selectedLocation))
                .toList();
        List<WorkOrder> visibleWorkOrders = workOrderRepository.findAll().stream()
                .filter(workOrder -> shopContextService.canAccessTenantResource(workOrder.getShop(), workOrder.getShopLocation()))
                .filter(workOrder -> matchesLocation(workOrder.getShopLocation(), selectedLocation))
                .toList();
        List<Expense> visibleExpenses = expenseRepository.findAll().stream()
                .filter(expense -> shopContextService.canAccessTenantResource(expense.getShop(), expense.getShopLocation()))
                .filter(expense -> matchesLocation(expense.getShopLocation(), selectedLocation))
                .toList();
        List<User> visibleCustomers = userRepository.findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER).stream()
                .filter(shopContextService::canAccessTenantUser)
                .filter(customer -> matchesLocation(customer.getShopLocation(), selectedLocation))
                .toList();

        int totalTires = visibleTires.stream()
                .mapToInt(Tire::getQuantity)
                .sum();
        long lowStockTires = visibleTires.stream()
                .filter(tire -> tire.getAvailableQuantity() <= 5)
                .count();
        long totalInvoices = visibleInvoices.size();
        BigDecimal totalInvoiced = visibleInvoices.stream()
                .map(invoice -> invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = visibleInvoices.stream()
                .map(this::collectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingBalance = visibleInvoices.stream()
                .filter(invoice -> !isClosedInvoice(invoice))
                .map(this::balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long partiallyPaidInvoices = visibleInvoices.stream()
                .filter(invoice -> "PARTIALLY_PAID".equalsIgnoreCase(invoice.getStatus()) || "PARTIAL".equalsIgnoreCase(invoice.getStatus()))
                .count();
        BigDecimal totalRevenue = totalCollected;
        BigDecimal totalPayrollCost = visibleExpenses.stream()
                .filter(expense -> expense.getCategoryKey() == ExpenseCategory.PAYROLL)
                .map(expense -> expense.getTotal() == null ? BigDecimal.ZERO : expense.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = visibleExpenses.stream()
                .map(expense -> expense.getTotal() == null ? BigDecimal.ZERO : expense.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long todayAppointments = visibleAppointments.stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.BOOKED)
                .count();
        long pendingWorkOrders = countWorkOrdersByStatus(visibleWorkOrders, WorkOrderStatus.PENDING);
        long inProgressWorkOrders = countWorkOrdersByStatus(visibleWorkOrders, WorkOrderStatus.IN_PROGRESS);
        long vehicleReadyWorkOrders = countWorkOrdersByStatus(visibleWorkOrders, WorkOrderStatus.VEHICLE_READY);
        long completedWorkOrdersToday = visibleWorkOrders.stream()
                .filter(workOrder -> workOrder.getStatus() == WorkOrderStatus.COMPLETED)
                .filter(workOrder -> workOrder.getCompletedAt() != null)
                .filter(workOrder -> !workOrder.getCompletedAt().isBefore(startOfDay)
                        && !workOrder.getCompletedAt().isAfter(endOfDay))
                .count();

        return new DashboardSummary(
                totalTires,
                (int) lowStockTires,
                totalInvoices,
                totalRevenue,
                totalInvoiced,
                totalCollected,
                outstandingBalance,
                partiallyPaidInvoices,
                todayAppointments,
                pendingWorkOrders,
                inProgressWorkOrders,
                vehicleReadyWorkOrders,
                completedWorkOrdersToday,
                visibleCustomers.size(),
                totalPayrollCost,
                totalExpenses,
                buildLocationBreakdowns()
        );
    }

    public List<SalesData> getRecentSales(int days) {
        return getRecentSales(days, null);
    }

    public List<SalesData> getRecentSales(int days, Long locationId) {
        ShopLocation selectedLocation = shopContextService.resolveAccessibleLocation(locationId, null, false).orElse(null);
        LocalDate startDate = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        Map<LocalDate, SalesBucket> salesByDate = new TreeMap<>();

        invoiceRepository.findAll().stream()
                .filter(invoice -> shopContextService.canAccessTenantResource(invoice.getShop(), invoice.getShopLocation()))
                .filter(invoice -> matchesLocation(invoice.getShopLocation(), selectedLocation))
                .filter(invoice -> invoice.getCreatedAt() != null)
                .filter(invoice -> !invoice.getCreatedAt().isBefore(startDate.atStartOfDay()))
                .sorted(Comparator.comparing(Invoice::getCreatedAt))
                .forEach(invoice -> salesByDate
                        .computeIfAbsent(invoice.getCreatedAt().toLocalDate(), date -> new SalesBucket())
                        .add(invoice.getAmountPaid()));

        return salesByDate.entrySet().stream()
                .map(entry -> new SalesData(entry.getKey(), entry.getValue().revenue(), entry.getValue().invoiceCount()))
                .toList();
    }

    private static class SalesBucket {
        private BigDecimal revenue = BigDecimal.ZERO;
        private long invoiceCount = 0;

        void add(BigDecimal invoiceTotal) {
            revenue = revenue.add(invoiceTotal == null ? BigDecimal.ZERO : invoiceTotal);
            invoiceCount += 1;
        }

        BigDecimal revenue() {
            return revenue;
        }

        long invoiceCount() {
            return invoiceCount;
        }
    }

    private long countWorkOrdersByStatus(List<WorkOrder> workOrders, WorkOrderStatus status) {
        return workOrders.stream()
                .filter(workOrder -> workOrder.getStatus() == status)
                .count();
    }

    private boolean isClosedInvoice(Invoice invoice) {
        String status = invoice.getStatus() == null ? "UNPAID" : invoice.getStatus();
        return "PAID".equalsIgnoreCase(status) || "VOID".equalsIgnoreCase(status);
    }

    private BigDecimal collectedAmount(Invoice invoice) {
        if (invoice.getAmountPaid() != null && invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            return invoice.getAmountPaid();
        }

        return "PAID".equalsIgnoreCase(invoice.getStatus())
                ? invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal()
                : BigDecimal.ZERO;
    }

    private BigDecimal balanceDue(Invoice invoice) {
        if (invoice.getBalanceDue() != null && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            return invoice.getBalanceDue();
        }

        BigDecimal total = invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal();
        return total.subtract(collectedAmount(invoice)).max(BigDecimal.ZERO);
    }

    private List<DashboardLocationBreakdown> buildLocationBreakdowns() {
        return accessibleActiveLocations().stream()
                .map(this::buildLocationBreakdown)
                .toList();
    }

    private DashboardLocationBreakdown buildLocationBreakdown(ShopLocation location) {
        List<Tire> tires = tireRepository.findAll().stream()
                .filter(tire -> shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation()))
                .filter(tire -> matchesLocation(tire.getShopLocation(), location))
                .toList();
        List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(invoice -> shopContextService.canAccessTenantResource(invoice.getShop(), invoice.getShopLocation()))
                .filter(invoice -> matchesLocation(invoice.getShopLocation(), location))
                .toList();
        LocalDateTime startOfDay = LocalDate.now().atTime(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay).stream()
                .filter(appointment -> shopContextService.canAccessTenantResource(appointment.getShop(), appointment.getShopLocation()))
                .filter(appointment -> matchesLocation(appointment.getShopLocation(), location))
                .toList();
        List<Expense> expenses = expenseRepository.findAll().stream()
                .filter(expense -> shopContextService.canAccessTenantResource(expense.getShop(), expense.getShopLocation()))
                .filter(expense -> matchesLocation(expense.getShopLocation(), location))
                .toList();
        long customers = userRepository.findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER).stream()
                .filter(shopContextService::canAccessTenantUser)
                .filter(customer -> matchesLocation(customer.getShopLocation(), location))
                .count();

        BigDecimal revenue = invoices.stream()
                .map(this::collectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstanding = invoices.stream()
                .filter(invoice -> !isClosedInvoice(invoice))
                .map(this::balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payrollCost = expenses.stream()
                .filter(expense -> expense.getCategoryKey() == ExpenseCategory.PAYROLL)
                .map(expense -> expense.getTotal() == null ? BigDecimal.ZERO : expense.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenseTotal = expenses.stream()
                .map(expense -> expense.getTotal() == null ? BigDecimal.ZERO : expense.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardLocationBreakdown(
                location.getId(),
                location.getName(),
                location.getType() == null ? null : location.getType().name(),
                tires.stream().mapToInt(Tire::getQuantity).sum(),
                tires.stream().filter(tire -> tire.getAvailableQuantity() <= 5).count(),
                appointments.stream().filter(appointment -> appointment.getStatus() == AppointmentStatus.BOOKED).count(),
                invoices.size(),
                customers,
                revenue,
                outstanding,
                payrollCost,
                expenseTotal);
    }

    private List<ShopLocation> accessibleActiveLocations() {
        if (shopContextService.isSuperAdmin()) {
            return shopLocationRepository.findAll().stream()
                    .filter(ShopLocation::isActive)
                    .toList();
        }

        return shopContextService.getCurrentTenantShopId()
                .map(shopLocationRepository::findByShop_IdAndActiveTrue)
                .orElse(List.of()).stream()
                .filter(location -> shopContextService.canAccessTenantResource(location.getShop(), location))
                .toList();
    }

    private boolean matchesLocation(ShopLocation resourceLocation, ShopLocation requestedLocation) {
        if (requestedLocation == null) {
            return true;
        }

        return resourceLocation != null
                && resourceLocation.getId() != null
                && resourceLocation.getId().equals(requestedLocation.getId());
    }
}
