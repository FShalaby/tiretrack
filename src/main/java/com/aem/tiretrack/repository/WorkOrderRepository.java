package com.aem.tiretrack.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.enums.WorkOrderStatus;
import com.aem.tiretrack.model.WorkOrder;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    @Override
    @EntityGraph(attributePaths = { "shop", "appointment", "customer", "assignedEmployee" })
    List<WorkOrder> findAll();

    @Override
    @EntityGraph(attributePaths = { "shop", "appointment", "customer", "assignedEmployee" })
    Optional<WorkOrder> findById(Long id);

    @EntityGraph(attributePaths = { "shop", "appointment", "customer", "assignedEmployee" })
    Optional<WorkOrder> findByAppointment_Id(Long appointmentId);

    @EntityGraph(attributePaths = { "shop", "appointment", "customer", "assignedEmployee" })
    List<WorkOrder> findByShop_Id(Long shopId);

    long countByStatus(WorkOrderStatus status);

    long countByShop_IdAndStatus(Long shopId, WorkOrderStatus status);

    long countByCompletedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByShop_IdAndCompletedAtBetween(Long shopId, LocalDateTime start, LocalDateTime end);
}
