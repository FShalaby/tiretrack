package com.aem.tiretrack.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.UserResponse;
import com.aem.tiretrack.dto.auth.LoginRequest;
import com.aem.tiretrack.dto.auth.LoginResponse;
import com.aem.tiretrack.dto.auth.RegisterRequest;
import com.aem.tiretrack.dto.auth.RefreshTokenRequest;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.RefreshToken;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.RefreshTokenRepository;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.security.JwtService;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccountValidationService accountValidationService;
    private final ShopContextService shopContextService;
    private final ShopRepository shopRepository;
    private final ShopLocationRepository shopLocationRepository;

    @Value("${refresh.token.expiration:604800000}")
    private long refreshTokenExpiration;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AccountValidationService accountValidationService,
            ShopContextService shopContextService,
            ShopRepository shopRepository,
            ShopLocationRepository shopLocationRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accountValidationService = accountValidationService;
        this.shopContextService = shopContextService;
        this.shopRepository = shopRepository;
        this.shopLocationRepository = shopLocationRepository;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String normalizedEmail = accountValidationService.normalizeEmail(request.getEmail());
        accountValidationService.validateNewAccount(normalizedEmail, request.getPassword());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already in use");
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(normalizedEmail);
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        applyCustomerShopSelection(user, request);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());
        String refreshToken = createRefreshToken(savedUser);
        return loginResponse(savedUser, "Registration successful", token, refreshToken);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(accountValidationService.normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user);
        return loginResponse(user, "Login successful", token, refreshToken);
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        User user = storedToken.getUser();
        if (!user.isActive()) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String token = jwtService.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user);
        refreshTokenRepository.delete(storedToken);

        return loginResponse(user, "Token refreshed", token, refreshToken);
    }

    public UserResponse currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getName();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authentication is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserResponse(user);
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration)));
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private LoginResponse loginResponse(User user, String message, String token, String refreshToken) {
        return new LoginResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                message,
                token,
                refreshToken,
                user.getShop() == null ? null : user.getShop().getId(),
                user.getShop() == null ? null : user.getShop().getName(),
                user.getShop() == null ? null : user.getShop().getSubscriptionPlan(),
                user.getShop() != null && user.getShop().hasMultiLocationAccess(),
                isShopOwner(user),
                user.getShopLocation() == null ? null : user.getShopLocation().getId(),
                user.getShopLocation() == null ? null : user.getShopLocation().getName(),
                shopContextService.getAccessibleLocationIds(user),
                permissionsFor(user));
    }

    private void applyCustomerShopSelection(User user, RegisterRequest request) {
        Shop shop = null;
        boolean platformHasActiveShops = !shopRepository.findByActiveTrueOrderByNameAsc().isEmpty();
        if (request.getShopId() == null) {
            if (platformHasActiveShops) {
                throw new IllegalArgumentException("Choose a shop before creating a customer account.");
            }
        } else {
            shop = shopRepository.findById(request.getShopId())
                    .filter(Shop::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
            user.setShop(shop);
        }

        if (shop != null && request.getLocationId() == null) {
            boolean hasPublicLocations = shopLocationRepository.findByShop_IdAndActiveTrueAndCustomerFacingTrue(shop.getId()).stream()
                    .anyMatch(shopContextService::canUseCustomerFacingLocation);
            if (hasPublicLocations) {
                throw new IllegalArgumentException("Choose a customer-facing location before creating a customer account.");
            }
        }

        if (request.getLocationId() != null) {
            ShopLocation location = shopContextService.resolveAccessibleLocation(request.getLocationId(), shop, true)
                    .orElseThrow(() -> new IllegalArgumentException("Shop location is required"));
            if (!shopContextService.canUseCustomerFacingLocation(location)) {
                throw new IllegalArgumentException("This location is not available for customer registration.");
            }
            user.setShopLocation(location);
            if (user.getShop() == null) {
                user.setShop(location.getShop());
            }
        }
    }

    private List<String> permissionsFor(User user) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return List.of("PLATFORM_MANAGE");
        }

        if (user.getRole() == UserRole.OWNER) {
            return List.of("SHOP_OWNER", "ALL_LOCATIONS", "OWNER_DASHBOARD");
        }

        if (user.getRole() == UserRole.ADMIN && isShopOwner(user)) {
            return List.of("SHOP_OWNER", "ALL_LOCATIONS", "LEGACY_ADMIN_OWNER");
        }

        if (user.getRole() == UserRole.ADMIN && user.getShopLocation() == null) {
            return List.of("SHOP_ADMIN");
        }

        if (user.getRole() == UserRole.ADMIN) {
            return List.of("LOCATION_ADMIN");
        }

        if (user.getRole() == UserRole.EMPLOYEE) {
            return List.of("EMPLOYEE_PORTAL");
        }

        return List.of("CUSTOMER_PORTAL");
    }

    private boolean isShopOwner(User user) {
        Shop shop = user.getShop();

        if (user.getRole() == UserRole.OWNER) {
            return true;
        }

        return shop != null
                && shop.getOwnerAdminId() != null
                && shop.getOwnerAdminId().equals(user.getId());
    }
}
