package com.aem.tiretrack.repository;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    @EntityGraph(attributePaths = {"shop", "shop.ownerAdmin", "shopLocation"})
    Optional<User> findById(Long id);
    @EntityGraph(attributePaths = {"shop", "shop.ownerAdmin", "shopLocation"})
    Optional<User> findByEmail(String email);
    @EntityGraph(attributePaths = {"shop", "shop.ownerAdmin", "shopLocation"})
    Optional<User> findByPhone(String phone);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);
    @EntityGraph(attributePaths = {"shop", "shop.ownerAdmin", "shopLocation"})
    List<User> findAllByOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = {"shop", "shop.ownerAdmin", "shopLocation"})
    List<User> findByRoleOrderByCreatedAtDesc(UserRole role);
    @EntityGraph(attributePaths = {"shop", "shop.ownerAdmin", "shopLocation"})
    List<User> findByRoleAndShop_IdOrderByCreatedAtDesc(UserRole role, Long shopId);
    @EntityGraph(attributePaths = {"shop", "shop.ownerAdmin", "shopLocation"})
    List<User> findByShopLocation_IdOrderByCreatedAtDesc(Long locationId);
}
