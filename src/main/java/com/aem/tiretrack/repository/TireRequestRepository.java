package com.aem.tiretrack.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.enums.TireRequestStatus;
import com.aem.tiretrack.model.TireRequest;

public interface TireRequestRepository extends JpaRepository<TireRequest, Long> {
    List<TireRequest> findByShop_IdOrderByCreatedAtDesc(Long shopId);
    List<TireRequest> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    List<TireRequest> findByShop_IdAndStatusOrderByCreatedAtDesc(Long shopId, TireRequestStatus status);
    List<TireRequest> findByStatusInOrderByCreatedAtDesc(Collection<TireRequestStatus> statuses);
    boolean existsByAppointment_Id(Long appointmentId);
}
