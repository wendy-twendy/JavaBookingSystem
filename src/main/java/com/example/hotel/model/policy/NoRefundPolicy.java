package com.example.hotel.model.policy;

import com.example.hotel.model.Booking;
import java.time.LocalDate;

/**
 * No refund policy - used for non-refundable rooms.
 */
public class NoRefundPolicy implements RefundPolicy {

    @Override
    public double calculateRefund(Booking booking, LocalDate cancelDate) {
        return 0.0;
    }

    @Override
    public String getDescription() {
        return "Non-refundable - no refund will be provided for cancellations.";
    }
}
