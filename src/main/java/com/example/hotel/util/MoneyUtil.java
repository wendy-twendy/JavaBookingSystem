package com.example.hotel.util;

import com.example.hotel.persistence.Settings;

import java.text.DecimalFormat;

/**
 * Utility class for money/currency operations.
 */
public final class MoneyUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.0");

    private MoneyUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Format an amount with currency symbol.
     */
    public static String formatCurrency(double amount) {
        String currency = Settings.getInstance().getCurrency();
        return currency + " " + DECIMAL_FORMAT.format(amount);
    }

    /**
     * Format an amount with specific currency.
     */
    public static String formatCurrency(double amount, String currencyCode) {
        return currencyCode + " " + DECIMAL_FORMAT.format(amount);
    }

    /**
     * Format an amount without currency symbol.
     */
    public static String formatAmount(double amount) {
        return DECIMAL_FORMAT.format(amount);
    }

    /**
     * Format a percentage.
     */
    public static String formatPercentage(double value) {
        return PERCENTAGE_FORMAT.format(value * 100) + "%";
    }

    /**
     * Format VAT rate for display.
     */
    public static String formatVatRate(double vatRate) {
        return formatPercentage(vatRate);
    }

    /**
     * Parse a currency string to double.
     * Removes currency symbols and formatting.
     */
    public static double parseCurrency(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }

        // Remove currency symbols and whitespace
        String cleaned = amountStr.replaceAll("[^0-9.,\\-]", "");

        // Handle different decimal separators
        cleaned = cleaned.replace(",", "");

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Calculate VAT amount.
     */
    public static double calculateVat(double subtotal, double vatRate) {
        return subtotal * vatRate;
    }

    /**
     * Calculate total with VAT.
     */
    public static double calculateTotal(double subtotal, double vatRate) {
        return subtotal + calculateVat(subtotal, vatRate);
    }

    /**
     * Round to 2 decimal places.
     */
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Check if an amount is valid (non-negative).
     */
    public static boolean isValidAmount(double amount) {
        return amount >= 0 && !Double.isNaN(amount) && !Double.isInfinite(amount);
    }

    /**
     * Check if a price string is valid.
     */
    public static boolean isValidPrice(String priceStr) {
        try {
            double price = parseCurrency(priceStr);
            return isValidAmount(price);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the default currency code.
     */
    public static String getDefaultCurrency() {
        return Settings.getInstance().getCurrency();
    }

    /**
     * Format subtotal breakdown for invoice display.
     */
    public static String formatSubtotalBreakdown(long nights, double pricePerNight) {
        return String.format("%d nights x %s = %s",
            nights,
            formatCurrency(pricePerNight),
            formatCurrency(nights * pricePerNight));
    }
}
