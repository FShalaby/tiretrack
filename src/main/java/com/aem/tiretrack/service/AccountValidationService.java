package com.aem.tiretrack.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class AccountValidationService {
    public static final String DEV_ADMIN_EMAIL = "fouad@test.com";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
    );

    private static final Pattern BCRYPT_HASH_PATTERN = Pattern.compile(
            "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$"
    );

    public String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public boolean isDevAdmin(String email) {
        return DEV_ADMIN_EMAIL.equalsIgnoreCase(normalizeEmail(email));
    }

    public boolean isValidEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return !normalizedEmail.contains("..")
                && EMAIL_PATTERN.matcher(normalizedEmail).matches();
    }

    public boolean isValidNewPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public boolean isValidStoredPasswordHash(String passwordHash) {
        return passwordHash != null && BCRYPT_HASH_PATTERN.matcher(passwordHash).matches();
    }

    public void validateNewAccount(String email, String password) {
        if (isDevAdmin(email)) {
            return;
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Enter a valid email address");
        }

        if (!isValidNewPassword(password)) {
            throw new IllegalArgumentException("Password must include uppercase, lowercase, number, and symbol");
        }
    }
}
