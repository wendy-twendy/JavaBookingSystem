package com.example.hotel.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date operations.
 */
public final class DateUtil {

    // Common date formats
    public static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_DISPLAY_FORMATTER =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATETIME_DISPLAY_FORMATTER =
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Format a LocalDate for display.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_DISPLAY_FORMATTER);
    }

    /**
     * Format a LocalDate in ISO format (yyyy-MM-dd).
     */
    public static String formatDateIso(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format a LocalDateTime for display.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATETIME_DISPLAY_FORMATTER);
    }

    /**
     * Parse a date string in ISO format.
     * @return The parsed date, or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Check if a date string is valid ISO format.
     */
    public static boolean isValidDate(String dateStr) {
        return parseDate(dateStr) != null;
    }

    /**
     * Calculate the number of nights between two dates.
     */
    public static long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return 0;
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return Math.max(0, nights);
    }

    /**
     * Check if a date is in the future (after today).
     */
    public static boolean isFutureDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }

    /**
     * Check if a date is today or in the future.
     */
    public static boolean isTodayOrFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(LocalDate.now());
    }

    /**
     * Check if a date is in the past (before today).
     */
    public static boolean isPastDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }

    /**
     * Validate that check-out is after check-in.
     */
    public static boolean isValidDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return false;
        }
        return checkOut.isAfter(checkIn);
    }

    /**
     * Get the number of days until a date.
     */
    public static long daysUntil(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    /**
     * Get today's date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Get tomorrow's date.
     */
    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    /**
     * Format a date range for display.
     */
    public static String formatDateRange(LocalDate start, LocalDate end) {
        return formatDate(start) + " - " + formatDate(end);
    }
}
