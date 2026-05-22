package com.aem.tiretrack.model;

import com.aem.tiretrack.enums.EmploymentType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EmploymentTypeConverter implements AttributeConverter<EmploymentType, String> {

    @Override
    public String convertToDatabaseColumn(EmploymentType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public EmploymentType convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase();

        try {
            return EmploymentType.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
