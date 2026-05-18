package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aem.tiretrack.dto.DashboardSummary;
import com.aem.tiretrack.dto.SalesData;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.TireRepository;

@Service
public class DashboardService 
{
    private final TireRepository tireRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    public DashboardService(TireRepository tireRepository, AppointmentRepository appointmentRepository, InvoiceRepository invoiceRepository) {
        this.tireRepository = tireRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public DashboardSummary getDashboardSummary() {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atTime(LocalTime.MIN);
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        int totalTires = tireRepository.getTotalQuantity();
        long lowStockTires = tireRepository.countLowStockTires(5);
        long totalInvoices = invoiceRepository.countTotalInvoices();
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenue();
        long todayAppointments = appointmentRepository.countTodayAppointments(startOfDay, endOfDay);

        return new DashboardSummary(
                totalTires,
                (int) lowStockTires,
                totalInvoices,
                totalRevenue,
                todayAppointments
        );
    }

    public List<SalesData> getRecentSales(int days) {
        LocalDate startDate = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        return invoiceRepository.getSalesSince(startDate.atStartOfDay()).stream()
                .map(row -> new SalesData(
                        toLocalDate(row[0]),
                        (BigDecimal) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Date date) {
            return date.toLocalDate();
        }

        if (value instanceof LocalDate date) {
            return date;
        }

        return LocalDate.parse(value.toString());
    }
}
