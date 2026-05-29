package com.aem.tiretrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.RefreshTokenRepository;
import com.aem.tiretrack.repository.UserRepository;

@Component
public class AccountValidationRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AccountValidationRunner.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountValidationService accountValidationService;

    public AccountValidationRunner(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, AccountValidationService accountValidationService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountValidationService = accountValidationService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (User user : userRepository.findAll()) {
            if (accountValidationService.isDevAdmin(user.getEmail())) {
                continue;
            }

            boolean hasValidEmail = accountValidationService.isValidEmail(user.getEmail());
            boolean hasValidPasswordHash = accountValidationService.isValidStoredPasswordHash(user.getPasswordHash());

            if (hasValidEmail && hasValidPasswordHash) {
                continue;
            }

            if (user.isActive()) {
                user.setActive(false);
                refreshTokenRepository.deleteByUser(user);
                log.warn("Disabled account {} because it has {}{}",
                        user.getId(),
                        hasValidEmail ? "" : "an invalid email",
                        hasValidPasswordHash ? "" : (hasValidEmail ? "an invalid password hash" : " and an invalid password hash"));
            }
        }
    }
}
