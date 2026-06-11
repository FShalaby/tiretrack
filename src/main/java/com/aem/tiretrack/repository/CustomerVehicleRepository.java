package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.User;

public interface CustomerVehicleRepository extends JpaRepository<CustomerVehicle, Long> {
    List<CustomerVehicle> findByCustomerOrderByCreatedAtDesc(User customer);
    Optional<CustomerVehicle> findByIdAndCustomer(Long id, User customer);
    long countByCustomer(User customer);

    @Query("""
            select distinct vehicle
            from CustomerVehicle vehicle
            join vehicle.customer owner
            where owner.id = :customerId
               or (:email is not null and lower(owner.email) = lower(:email))
               or (:phone is not null and owner.phone = :phone)
            order by vehicle.createdAt desc
            """)
    List<CustomerVehicle> findPortalVehicles(
            @Param("customerId") Long customerId,
            @Param("email") String email,
            @Param("phone") String phone);
}
