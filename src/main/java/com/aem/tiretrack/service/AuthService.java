package com.aem.tiretrack.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aem.tiretrack.dto.auth.LoginRequest;
import com.aem.tiretrack.dto.auth.LoginResponse;
import com.aem.tiretrack.dto.auth.RegisterRequest;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.model.User;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse  register(RegisterRequest request)
    {
       if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already in use");
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        return new LoginResponse(savedUser.getId(),savedUser.getFullName(),savedUser.getEmail(),savedUser.getRole(),"Registration successful");
    }

    public LoginResponse login(LoginRequest request)
    {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
        {
            throw new IllegalArgumentException("Invalid email or password");

        }
        return new LoginResponse(user.getId(),user.getFullName(),user.getEmail(),user.getRole(),"Login Successful");
    }
}
