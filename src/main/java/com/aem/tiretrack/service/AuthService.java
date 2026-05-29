package com.aem.tiretrack.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.auth.LoginRequest;
import com.aem.tiretrack.dto.auth.LoginResponse;
import com.aem.tiretrack.dto.auth.RegisterRequest;
import com.aem.tiretrack.dto.auth.RefreshTokenRequest;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.RefreshToken;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.RefreshTokenRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.security.JwtService;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccountValidationService accountValidationService;

    @Value("${refresh.token.expiration:604800000}")
    private long refreshTokenExpiration;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AccountValidationService accountValidationService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accountValidationService = accountValidationService;
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

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());
        String refreshToken = createRefreshToken(savedUser);
        return new LoginResponse(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail(), savedUser.getRole(), "Registration successful", token, refreshToken);
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
        return new LoginResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), "Login successful", token, refreshToken);
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

        return new LoginResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), "Token refreshed", token, refreshToken);
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration)));
        return refreshTokenRepository.save(refreshToken).getToken();
    }
}
