package com.example.hotel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil.
 */
class ValidationUtilTest {

    // Email Validation Tests

    @Test
    @DisplayName("Should accept valid email formats")
    void testValidEmail() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user+tag@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user_name@example.co.uk"));
        assertTrue(ValidationUtil.isValidEmail("user123@example.org"));
    }

    @Test
    @DisplayName("Should reject invalid email formats")
    void testInvalidEmail() {
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail("user"));
        assertFalse(ValidationUtil.isValidEmail("user@"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
        assertFalse(ValidationUtil.isValidEmail("user@example"));
        assertFalse(ValidationUtil.isValidEmail("user example@test.com"));
        assertFalse(ValidationUtil.isValidEmail("   "));
    }

    // Phone Validation Tests

    @Test
    @DisplayName("Should accept valid phone formats")
    void testValidPhone() {
        assertTrue(ValidationUtil.isValidPhone("5551234567"));
        assertTrue(ValidationUtil.isValidPhone("+15551234567"));
        assertTrue(ValidationUtil.isValidPhone("555-123-4567")); // Dashes are stripped
        assertTrue(ValidationUtil.isValidPhone("555 123 4567")); // Spaces are stripped
        assertTrue(ValidationUtil.isValidPhone("1234567")); // Minimum 7 digits
        assertTrue(ValidationUtil.isValidPhone("+441234567890123")); // Up to 15 digits
    }

    @Test
    @DisplayName("Should reject invalid phone formats")
    void testInvalidPhone() {
        assertFalse(ValidationUtil.isValidPhone(""));
        assertFalse(ValidationUtil.isValidPhone(null));
        assertFalse(ValidationUtil.isValidPhone("123456")); // Too short (less than 7)
        assertFalse(ValidationUtil.isValidPhone("abcdefghij")); // Letters
        assertFalse(ValidationUtil.isValidPhone("   "));
    }

    // Room Number Validation Tests

    @Test
    @DisplayName("Should accept valid room number formats")
    void testValidRoomNumber() {
        assertTrue(ValidationUtil.isValidRoomNumber("101"));
        assertTrue(ValidationUtil.isValidRoomNumber("A101"));
        assertTrue(ValidationUtil.isValidRoomNumber("Suite-1"));
        assertTrue(ValidationUtil.isValidRoomNumber("PENT-01"));
        assertTrue(ValidationUtil.isValidRoomNumber("room1"));
    }

    @Test
    @DisplayName("Should reject invalid room number formats")
    void testInvalidRoomNumber() {
        assertFalse(ValidationUtil.isValidRoomNumber(""));
        assertFalse(ValidationUtil.isValidRoomNumber(null));
        assertFalse(ValidationUtil.isValidRoomNumber("   "));
        assertFalse(ValidationUtil.isValidRoomNumber("Room 101")); // Space not allowed
        assertFalse(ValidationUtil.isValidRoomNumber("Room@101")); // Special chars not allowed
    }

    // Name Validation Tests

    @Test
    @DisplayName("Should accept valid name lengths")
    void testValidName() {
        assertTrue(ValidationUtil.isValidName("Jo")); // Minimum 2 chars
        assertTrue(ValidationUtil.isValidName("John"));
        assertTrue(ValidationUtil.isValidName("John Doe"));
        assertTrue(ValidationUtil.isValidName("Jean-Pierre"));

        // 100 character name (max)
        String longName = "A".repeat(100);
        assertTrue(ValidationUtil.isValidName(longName));
    }

    @Test
    @DisplayName("Should reject invalid names")
    void testInvalidName() {
        assertFalse(ValidationUtil.isValidName(""));
        assertFalse(ValidationUtil.isValidName(null));
        assertFalse(ValidationUtil.isValidName("J")); // Too short (1 char)
        assertFalse(ValidationUtil.isValidName("   ")); // Only whitespace

        // 101 character name (over max)
        String tooLongName = "A".repeat(101);
        assertFalse(ValidationUtil.isValidName(tooLongName));
    }

    // Booking Dates Validation Tests

    @Test
    @DisplayName("Should accept valid booking dates")
    void testValidateBookingDates() {
        LocalDate today = LocalDate.now();
        LocalDate checkIn = today.plusDays(1);
        LocalDate checkOut = today.plusDays(3);

        ValidationUtil.ValidationResult result = ValidationUtil.validateBookingDates(checkIn, checkOut);

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should accept same-day check-in")
    void testValidateBookingDatesToday() {
        LocalDate today = LocalDate.now();
        LocalDate checkOut = today.plusDays(1);

        ValidationUtil.ValidationResult result = ValidationUtil.validateBookingDates(today, checkOut);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject past check-in date")
    void testValidateBookingDatesPastCheckIn() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        ValidationUtil.ValidationResult result = ValidationUtil.validateBookingDates(pastDate, checkOut);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("past"));
    }

    @Test
    @DisplayName("Should reject check-out not after check-in")
    void testValidateBookingDatesInvalidRange() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(3); // Before check-in

        ValidationUtil.ValidationResult result = ValidationUtil.validateBookingDates(checkIn, checkOut);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("after"));
    }

    @Test
    @DisplayName("Should reject same check-in and check-out dates")
    void testValidateBookingDatesSameDay() {
        LocalDate sameDate = LocalDate.now().plusDays(1);

        ValidationUtil.ValidationResult result = ValidationUtil.validateBookingDates(sameDate, sameDate);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("after"));
    }

    @Test
    @DisplayName("Should reject null check-in date")
    void testValidateBookingDatesNullCheckIn() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateBookingDates(null, LocalDate.now().plusDays(3));

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Check-in"));
    }

    @Test
    @DisplayName("Should reject null check-out date")
    void testValidateBookingDatesNullCheckOut() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateBookingDates(LocalDate.now().plusDays(1), null);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Check-out"));
    }

    // Guest Validation Tests

    @Test
    @DisplayName("Should validate valid guest information")
    void testValidateGuest() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateGuest(
            "John Doe",
            "5551234567",
            "john@example.com"
        );

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject guest with invalid name")
    void testValidateGuestInvalidName() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateGuest(
            "J", // Too short
            "5551234567",
            "john@example.com"
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Name"));
    }

    @Test
    @DisplayName("Should reject guest with invalid phone")
    void testValidateGuestInvalidPhone() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateGuest(
            "John Doe",
            "123", // Too short
            "john@example.com"
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("phone"));
    }

    @Test
    @DisplayName("Should reject guest with invalid email")
    void testValidateGuestInvalidEmail() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateGuest(
            "John Doe",
            "5551234567",
            "invalid-email" // No @ symbol
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("email"));
    }

    // Price Validation Tests

    @Test
    @DisplayName("Should accept valid prices")
    void testValidPrice() {
        assertTrue(ValidationUtil.isValidPrice(0.01));
        assertTrue(ValidationUtil.isValidPrice(100.0));
        assertTrue(ValidationUtil.isValidPrice(999999.99));
    }

    @Test
    @DisplayName("Should reject invalid prices")
    void testInvalidPrice() {
        assertFalse(ValidationUtil.isValidPrice(0));
        assertFalse(ValidationUtil.isValidPrice(-1.0));
        assertFalse(ValidationUtil.isValidPrice(-100.0));
        assertFalse(ValidationUtil.isValidPrice(Double.NaN));
        assertFalse(ValidationUtil.isValidPrice(Double.POSITIVE_INFINITY));
        assertFalse(ValidationUtil.isValidPrice(Double.NEGATIVE_INFINITY));
    }

    @Test
    @DisplayName("Should validate non-negative amounts")
    void testNonNegative() {
        assertTrue(ValidationUtil.isNonNegative(0));
        assertTrue(ValidationUtil.isNonNegative(0.0));
        assertTrue(ValidationUtil.isNonNegative(100.0));
        assertFalse(ValidationUtil.isNonNegative(-0.01));
        assertFalse(ValidationUtil.isNonNegative(Double.NaN));
    }

    // Room Validation Tests

    @Test
    @DisplayName("Should validate valid room information")
    void testValidateRoom() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateRoom("101", 100.0);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject room with invalid room number")
    void testValidateRoomInvalidNumber() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateRoom("", 100.0);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Room number"));
    }

    @Test
    @DisplayName("Should reject room with invalid price")
    void testValidateRoomInvalidPrice() {
        ValidationUtil.ValidationResult result = ValidationUtil.validateRoom("101", -50.0);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Price"));
    }

    // Utility Method Tests

    @Test
    @DisplayName("Should check empty strings correctly")
    void testIsEmpty() {
        assertTrue(ValidationUtil.isEmpty(null));
        assertTrue(ValidationUtil.isEmpty(""));
        assertTrue(ValidationUtil.isEmpty("   "));
        assertFalse(ValidationUtil.isEmpty("text"));
        assertFalse(ValidationUtil.isEmpty(" text "));
    }

    @Test
    @DisplayName("Should check non-empty strings correctly")
    void testIsNotEmpty() {
        assertFalse(ValidationUtil.isNotEmpty(null));
        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty("   "));
        assertTrue(ValidationUtil.isNotEmpty("text"));
        assertTrue(ValidationUtil.isNotEmpty(" text "));
    }

    @Test
    @DisplayName("Should clean strings correctly")
    void testClean() {
        assertEquals("", ValidationUtil.clean(null));
        assertEquals("", ValidationUtil.clean(""));
        assertEquals("text", ValidationUtil.clean(" text "));
        assertEquals("hello world", ValidationUtil.clean("  hello world  "));
    }

    @Test
    @DisplayName("Should clean phone numbers correctly")
    void testCleanPhone() {
        assertEquals("", ValidationUtil.cleanPhone(null));
        assertEquals("5551234567", ValidationUtil.cleanPhone("555-123-4567"));
        assertEquals("5551234567", ValidationUtil.cleanPhone("555 123 4567"));
        assertEquals("5551234567", ValidationUtil.cleanPhone("555-123 4567"));
    }
}
