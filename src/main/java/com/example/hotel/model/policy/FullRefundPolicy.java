package com.example.hotel.model.policy;

import com.example.hotel.model.Booking;
import java.time.LocalDate;

/**
 * Full refund policy - always refunds the full amount.
 */
public class FullRefundPolicy extends AbstractRefundPolicy {

    public FullRefundPolicy() {
        super("Full Refund");
    }

    @Override
    public double calculateRefund(Booking booking, LocalDate cancelDate) {
        return booking.getTotalCost();
    }

    @Override
    public String getDescription() {
        return "Full refund - 100% of the booking cost will be refunded.";
    }
}
