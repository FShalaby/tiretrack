package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.User;

public interface CustomerVehicleRepository extends JpaRepository<CustomerVehicle, Long> {
    List<CustomerVehicle> findByCustomerOrderByCreatedAtDesc(User customer);
    Optional<CustomerVehicle> findByIdAndCustomer(Long id, User customer);
    long countByCustomer(User customer);
}
