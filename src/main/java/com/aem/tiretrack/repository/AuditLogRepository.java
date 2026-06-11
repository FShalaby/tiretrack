package com.aem.tiretrack.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aem.tiretrack.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop25ByOrderByCreatedAtDesc();
    List<AuditLog> findTop25ByShop_IdOrderByCreatedAtDesc(Long shopId);

    @Query("""
            select log
            from AuditLog log
            where log.shop is null or log.shop.id = :shopId
            order by log.createdAt desc
            """)
    List<AuditLog> findLatestVisibleForShop(@Param("shopId") Long shopId, Pageable pageable);

    @Modifying
    @Query("update AuditLog log set log.performedBy = 'legacy-system' where log.performedBy is null or log.performedBy = 'system'")
    int relabelLegacySystemActors();
}
