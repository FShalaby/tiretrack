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

import com.aem.tiretrack.dto.DashboardSummary;
import com.aem.tiretrack.dto.SalesData;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.WorkOrderStatus;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.WorkOrder;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.WorkOrderRepository;

@Service
public class DashboardService 
{
    private final TireRepository tireRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ShopContextService shopContextService;

    public DashboardService(TireRepository tireRepository, AppointmentRepository appointmentRepository, InvoiceRepository invoiceRepository, WorkOrderRepository workOrderRepository, ShopContextService shopContextService) {
        this.tireRepository = tireRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.shopContextService = shopContextService;
    }

    public DashboardSummary getDashboardSummary() {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atTime(LocalTime.MIN);
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Tire> visibleTires = tireRepository.findAll().stream()
                .filter(tire -> shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation()))
                .toList();
        List<Invoice> visibleInvoices = invoiceRepository.findAll().stream()
                .filter(invoice -> shopContextService.canAccessTenantResource(invoice.getShop(), invoice.getShopLocation()))
                .toList();
        List<Appointment> visibleAppointments = appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay).stream()
                .filter(appointment -> shopContextService.canAccessTenantResource(appointment.getShop(), appointment.getShopLocation()))
                .toList();
        List<WorkOrder> visibleWorkOrders = workOrderRepository.findAll().stream()
                .filter(workOrder -> shopContextService.canAccessTenantResource(workOrder.getShop(), workOrder.getShopLocation()))
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
                completedWorkOrdersToday
        );
    }

    public List<SalesData> getRecentSales(int days) {
        LocalDate startDate = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        Map<LocalDate, SalesBucket> salesByDate = new TreeMap<>();

        invoiceRepository.findAll().stream()
                .filter(invoice -> shopContextService.canAccessTenantResource(invoice.getShop(), invoice.getShopLocation()))
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
}
