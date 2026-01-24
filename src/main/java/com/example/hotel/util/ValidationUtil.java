package com.example.hotel.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public final class ValidationUtil {

    // Validation patterns
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9]{7,15}$");
    private static final Pattern ROOM_NUMBER_PATTERN =
        Pattern.compile("^[A-Za-z0-9-]+$");

    private ValidationUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Check if a string is null or empty.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number format.
     * Accepts 7-15 digits, optionally starting with +
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        // Remove spaces and dashes for validation
        String cleaned = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    /**
     * Validate room number format.
     * Allows alphanumeric characters and dashes.
     */
    public static boolean isValidRoomNumber(String roomNumber) {
        if (isEmpty(roomNumber)) {
            return false;
        }
        return ROOM_NUMBER_PATTERN.matcher(roomNumber.trim()).matches();
    }

    /**
     * Validate a name (not empty, reasonable length).
     */
    public static boolean isValidName(String name) {
        if (isEmpty(name)) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100;
    }

    /**
     * Validate a positive price.
     */
    public static boolean isValidPrice(double price) {
        return price > 0 && !Double.isNaN(price) && !Double.isInfinite(price);
    }

    /**
     * Validate a non-negative amount.
     */
    public static boolean isNonNegative(double amount) {
        return amount >= 0 && !Double.isNaN(amount) && !Double.isInfinite(amount);
    }

    /**
     * Validate booking dates.
     * Check-in must be today or future, check-out must be after check-in.
     */
    public static ValidationResult validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null) {
            return ValidationResult.error("Check-in date is required");
        }
        if (checkOut == null) {
            return ValidationResult.error("Check-out date is required");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            return ValidationResult.error("Check-in date cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            return ValidationResult.error("Check-out must be after check-in");
        }
        return ValidationResult.valid();
    }

    /**
     * Validate guest information.
     */
    public static ValidationResult validateGuest(String name, String phone, String email) {
        if (!isValidName(name)) {
            return ValidationResult.error("Name is required (2-100 characters)");
        }
        if (!isValidPhone(phone)) {
            return ValidationResult.error("Invalid phone number format");
        }
        if (!isValidEmail(email)) {
            return ValidationResult.error("Invalid email format");
        }
        return ValidationResult.valid();
    }

    /**
     * Validate room information.
     */
    public static ValidationResult validateRoom(String roomNumber, double price) {
        if (!isValidRoomNumber(roomNumber)) {
            return ValidationResult.error("Room number is required (alphanumeric only)");
        }
        if (!isValidPrice(price)) {
            return ValidationResult.error("Price must be a positive number");
        }
        return ValidationResult.valid();
    }

    /**
     * Trim and clean a string (null-safe).
     */
    public static String clean(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * Clean a phone number (remove spaces and dashes).
     */
    public static String cleanPhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[\\s-]", "");
    }

    /**
     * Result of a validation operation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
