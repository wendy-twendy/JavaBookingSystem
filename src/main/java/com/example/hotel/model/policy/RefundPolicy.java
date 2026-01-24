package com.example.hotel.model.policy;

import com.example.hotel.model.Booking;
import java.time.LocalDate;

/**
 * Interface for calculating refunds on booking cancellations.
 * Demonstrates polymorphism - different implementations provide different refund strategies.
 */
public interface RefundPolicy {

    /**
     * Calculate the refund amount for a cancelled booking.
     *
     * @param booking The booking being cancelled
     * @param cancelDate The date of cancellation
     * @return The refund amount
     */
    double calculateRefund(Booking booking, LocalDate cancelDate);

    /**
     * Get a description of this refund policy.
     */
    String getDescription();
}
