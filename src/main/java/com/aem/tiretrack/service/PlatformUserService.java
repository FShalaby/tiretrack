package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.PlatformUserRequest;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.RefreshTokenRepository;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class PlatformUserService {
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ShopLocationRepository shopLocationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountValidationService accountValidationService;

    public PlatformUserService(
            UserRepository userRepository,
            ShopRepository shopRepository,
            ShopLocationRepository shopLocationRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AccountValidationService accountValidationService) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.shopLocationRepository = shopLocationRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountValidationService = accountValidationService;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public User createUser(PlatformUserRequest request) {
        String normalizedEmail = accountValidationService.normalizeEmail(request.getEmail());
        accountValidationService.validateNewAccount(normalizedEmail, request.getPassword());

        if (request.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN accounts are platform-owner accounts and cannot be created from shop setup.");
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }
        String normalizedPhone = request.getPhone().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new IllegalArgumentException("Phone number already in use");
        }

        ShopAssignment assignment = resolveAssignment(request.getShopId(), request.getLocationId());
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(request.getActive() == null || request.getActive());
        user.setShop(assignment.shop());
        user.setShopLocation(assignment.location());

        if (request.getRole() == UserRole.EMPLOYEE) {
            user.setHourlyRate(request.getHourlyRate());
            user.setPayrollEnabled(Boolean.TRUE.equals(request.getPayrollEnabled()));
            user.setEmploymentType(request.getEmploymentType());
        }

        User savedUser = userRepository.save(user);

        if (savedUser.isActive() && savedUser.getRole() == UserRole.ADMIN && savedUser.getShop() != null && savedUser.getShopLocation() == null) {
            Shop shop = savedUser.getShop();
            shop.setOwnerAdmin(savedUser);
            shopRepository.save(shop);
        }

        return savedUser;
    }

    @Transactional
    public User activateUser(Long userId) {
        User user = getUser(userId);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public User deactivateUser(Long userId) {
        User user = getUser(userId);

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN accounts cannot be deactivated from shop setup.");
        }

        String currentEmail = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getName();

        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }

        user.setActive(false);
        refreshTokenRepository.deleteByUser(user);
        return userRepository.save(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    private ShopAssignment resolveAssignment(Long shopId, Long locationId) {
        Shop shop = null;
        ShopLocation location = null;

        if (locationId != null) {
            location = shopLocationRepository.findById(locationId)
                    .orElseThrow(() -> new IllegalArgumentException("Shop location not found with id: " + locationId));

            if (!location.isActive()) {
                throw new IllegalArgumentException("Cannot assign users to an inactive location");
            }

            shop = location.getShop();
        }

        if (shopId != null) {
            Shop requestedShop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));

            if (!requestedShop.isActive()) {
                throw new IllegalArgumentException("Cannot assign users to an inactive shop");
            }

            if (shop != null && !shop.getId().equals(requestedShop.getId())) {
                throw new IllegalArgumentException("Location does not belong to the selected shop");
            }

            shop = requestedShop;
        }

        return new ShopAssignment(shop, location);
    }

    private record ShopAssignment(Shop shop, ShopLocation location) {}
}
