package com.aem.tiretrack.model;

import com.aem.tiretrack.enums.UserRole;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .replace("ROLE_", "")
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase();

        return UserRole.valueOf(normalized);
    }
}
