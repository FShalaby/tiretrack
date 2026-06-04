package com.aem.tiretrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByName(String name);
}
