package com.example.hotel.model.policy;

import com.example.hotel.model.Booking;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Tiered refund policy based on days before check-in:
 * - 7+ days: 100% refund
 * - 3-6 days: 50% refund
 * - Less than 3 days: No refund
 */
public class TieredRefundPolicy implements RefundPolicy {

    private static final int FULL_REFUND_DAYS = 7;
    private static final int PARTIAL_REFUND_DAYS = 3;
    private static final double PARTIAL_REFUND_RATE = 0.50;

    @Override
    public double calculateRefund(Booking booking, LocalDate cancelDate) {
        LocalDate checkInDate = booking.getCheckInDate();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(cancelDate, checkInDate);

        if (daysUntilCheckIn >= FULL_REFUND_DAYS) {
            // 7 or more days before check-in: 100% refund
            return booking.getTotalCost();
        } else if (daysUntilCheckIn >= PARTIAL_REFUND_DAYS) {
            // 3-6 days before check-in: 50% refund
            return booking.getTotalCost() * PARTIAL_REFUND_RATE;
        } else {
            // Less than 3 days: no refund
            return 0.0;
        }
    }

    @Override
    public String getDescription() {
        return "Tiered refund: 100% if cancelled 7+ days before, " +
               "50% if 3-6 days before, no refund if less than 3 days.";
    }
}
