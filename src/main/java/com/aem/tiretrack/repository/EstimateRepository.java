package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aem.tiretrack.model.Estimate;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    @Override
    @EntityGraph(attributePaths = { "shop", "customer", "items" })
    List<Estimate> findAll();

    @Override
    @EntityGraph(attributePaths = { "shop", "customer", "items" })
    Optional<Estimate> findById(Long id);

    @EntityGraph(attributePaths = { "shop", "customer", "items" })
    Optional<Estimate> findByEstimateNumber(String estimateNumber);

    @EntityGraph(attributePaths = { "shop", "customer", "items" })
    List<Estimate> findByShop_Id(Long shopId);

    @EntityGraph(attributePaths = { "shop", "customer", "items" })
    @Query("""
            SELECT DISTINCT estimate FROM Estimate estimate
            LEFT JOIN estimate.customer customer
            WHERE (:customerId IS NOT NULL AND customer.id = :customerId)
               OR (:phone IS NOT NULL AND estimate.phone = :phone)
               OR (:email IS NOT NULL AND lower(estimate.email) = lower(:email))
               OR (:customerName IS NOT NULL AND lower(estimate.customerName) = lower(:customerName))
            """)
    List<Estimate> findCustomerHistory(
            @Param("customerId") Long customerId,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("customerName") String customerName);
}
