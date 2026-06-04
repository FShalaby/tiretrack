package com.aem.tiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findAllByOrderByNameAsc();
    List<Vendor> findByShop_IdOrderByNameAsc(Long shopId);
}
