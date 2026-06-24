package com.aem.tiretrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class PlatformOwnerBootstrapRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(PlatformOwnerBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountValidationService accountValidationService;

    @Value("${tiretrack.platform-owner.bootstrap-enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${tiretrack.platform-owner.email:fouad99877@gmail.com}")
    private String ownerEmail;

    @Value("${tiretrack.platform-owner.full-name:Fouad Shalaby}")
    private String ownerFullName;

    @Value("${tiretrack.platform-owner.phone:000-000-0000}")
    private String ownerPhone;

    @Value("${tiretrack.platform-owner.password:}")
    private String ownerPassword;

    @Value("${tiretrack.platform-owner.reset-password:false}")
    private boolean resetPassword;

    public PlatformOwnerBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AccountValidationService accountValidationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountValidationService = accountValidationService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled) {
            return;
        }

        String email = accountValidationService.normalizeEmail(ownerEmail);
        if (!accountValidationService.isValidEmail(email)) {
            throw new IllegalStateException("Platform owner email must be valid.");
        }

        userRepository.findByEmail(email).ifPresentOrElse(
                this::repairPlatformOwner,
                () -> createPlatformOwner(email));
    }

    private void repairPlatformOwner(User owner) {
        boolean changed = false;

        if (owner.getRole() != UserRole.SUPER_ADMIN) {
            owner.setRole(UserRole.SUPER_ADMIN);
            changed = true;
        }

        if (!owner.isActive()) {
            owner.setActive(true);
            changed = true;
        }

        if (owner.getShop() != null) {
            owner.setShop(null);
            changed = true;
        }

        if (owner.getShopLocation() != null) {
            owner.setShopLocation(null);
            changed = true;
        }

        if (owner.getFullName() == null || owner.getFullName().isBlank()) {
            owner.setFullName(ownerFullName);
            changed = true;
        }

        boolean invalidPasswordHash = !accountValidationService.isValidStoredPasswordHash(owner.getPasswordHash());
        if (resetPassword || invalidPasswordHash) {
            if (ownerPassword == null || ownerPassword.isBlank()) {
                throw new IllegalStateException("Set PLATFORM_OWNER_PASSWORD to repair or reset the platform owner password.");
            }
            accountValidationService.validateNewAccount(owner.getEmail(), ownerPassword);
            owner.setPasswordHash(passwordEncoder.encode(ownerPassword));
            changed = true;
        }

        if (changed) {
            userRepository.save(owner);
            logger.info("Platform owner SUPER_ADMIN account verified and repaired: {}", owner.getEmail());
        } else {
            logger.info("Platform owner SUPER_ADMIN account verified: {}", owner.getEmail());
        }
    }

    private void createPlatformOwner(String email) {
        if (ownerPassword == null || ownerPassword.isBlank()) {
            throw new IllegalStateException("Set PLATFORM_OWNER_PASSWORD to create the platform owner SUPER_ADMIN account.");
        }

        accountValidationService.validateNewAccount(email, ownerPassword);

        if (userRepository.existsByPhone(ownerPhone)) {
            throw new IllegalStateException("Platform owner phone number is already in use. Set PLATFORM_OWNER_PHONE to a unique value.");
        }

        User owner = new User();
        owner.setFullName(ownerFullName);
        owner.setEmail(email);
        owner.setPhone(ownerPhone);
        owner.setPasswordHash(passwordEncoder.encode(ownerPassword));
        owner.setRole(UserRole.SUPER_ADMIN);
        owner.setActive(true);
        owner.setShop(null);
        owner.setShopLocation(null);
        userRepository.save(owner);
        logger.info("Created platform owner SUPER_ADMIN account: {}", email);
    }
}
