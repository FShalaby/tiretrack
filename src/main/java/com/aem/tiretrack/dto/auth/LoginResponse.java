package com.aem.tiretrack.dto.auth;

import com.aem.tiretrack.enums.UserRole;

public class LoginResponse {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String message;
    private String token;
    private String refreshToken;

    public LoginResponse(Long id, String fullName, String email, UserRole role, String message, String token, String refreshToken) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}