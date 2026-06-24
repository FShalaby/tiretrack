package com.aem.tiretrack.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TireSizeUtils {
    private static final Pattern PASSENGER_SIZE_PATTERN = Pattern.compile("^(\\d{3})\\s*/\\s*(\\d{2})\\s*(?:R|/)?\\s*(\\d{2})$", Pattern.CASE_INSENSITIVE);

    private TireSizeUtils() {
    }

    public record TireSizeSpec(int width, int aspectRatio, int rimSize) {
        public String display() {
            return width + "/" + aspectRatio + "/" + rimSize;
        }
    }

    public static String formatPassengerSize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String lower = trimmed.toLowerCase();
        if (lower.startsWith("front:") && lower.contains("rear:")) {
            String withoutFront = trimmed.substring(trimmed.indexOf(':') + 1).trim();
            String[] parts = withoutFront.split("(?i)\\s*/\\s*Rear:\\s*", 2);
            if (parts.length == 2) {
                return "Front: " + formatPassengerSize(parts[0]) + " / Rear: " + formatPassengerSize(parts[1]);
            }
        }

        String digits = trimmed.replaceAll("\\D", "");
        if (digits.length() == 7) {
            return digits.substring(0, 3)
                    + "/"
                    + digits.substring(3, 5)
                    + "/"
                    + digits.substring(5);
        }

        return trimmed.replaceAll("(?i)^(\\d{3})/(\\d{2})R(\\d{2})$", "$1/$2/$3");
    }

    public static Optional<TireSizeSpec> parsePassengerSize(String value) {
        String formatted = formatPassengerSize(value);
        if (formatted == null || formatted.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = PASSENGER_SIZE_PATTERN.matcher(formatted.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(new TireSizeSpec(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))));
    }
}
