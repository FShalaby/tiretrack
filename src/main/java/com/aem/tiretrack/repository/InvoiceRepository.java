package com.aem.tiretrack.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aem.tiretrack.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> 
{
    @Query("SELECT COUNT(i) FROM Invoice i")
    long countTotalInvoices();
    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i")
    BigDecimal getTotalRevenue();

    @Query(value = """
            SELECT DATE(created_at) AS sale_date,
                   COALESCE(SUM(total), 0) AS revenue,
                   COUNT(*) AS invoice_count
            FROM invoices
            WHERE created_at >= :startDate
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
            """, nativeQuery = true)
    List<Object[]> getSalesSince(LocalDateTime startDate);

    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("select i from Invoice i where i.customerId = :customerId or i.phone = :phone order by i.createdAt desc")
    List<Invoice> findCustomerHistory(@Param("customerId") Long customerId, @Param("phone") String phone);
}
