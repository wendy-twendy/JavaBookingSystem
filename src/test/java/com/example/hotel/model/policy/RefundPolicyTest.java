package com.example.hotel.model.policy;

import com.example.hotel.model.Booking;
import com.example.hotel.model.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RefundPolicy implementations.
 * Tests polymorphism with NoRefundPolicy, FullRefundPolicy, and TieredRefundPolicy.
 */
class RefundPolicyTest {

    private Booking createTestBooking(LocalDate checkIn, double totalCost) {
        return new Booking(
            "TEST-BK001",
            "TEST-G001",
            "TEST-101",
            checkIn,
            checkIn.plusDays(3),
            BookingStatus.CONFIRMED,
            totalCost
        );
    }

    // NoRefundPolicy Tests

    @Test
    @DisplayName("NoRefundPolicy should always return 0")
    void testNoRefundPolicy() {
        RefundPolicy policy = new NoRefundPolicy();
        Booking booking = createTestBooking(LocalDate.now().plusDays(30), 500.0);

        double refund = policy.calculateRefund(booking, LocalDate.now());

        assertEquals(0.0, refund, "NoRefundPolicy should return 0 regardless of timing");
    }

    @Test
    @DisplayName("NoRefundPolicy should return 0 even for same-day cancellation")
    void testNoRefundPolicySameDay() {
        RefundPolicy policy = new NoRefundPolicy();
        LocalDate checkIn = LocalDate.now().plusDays(1);
        Booking booking = createTestBooking(checkIn, 1000.0);

        double refund = policy.calculateRefund(booking, checkIn);

        assertEquals(0.0, refund);
    }

    @Test
    @DisplayName("NoRefundPolicy description should mention non-refundable")
    void testNoRefundPolicyDescription() {
        RefundPolicy policy = new NoRefundPolicy();

        assertTrue(policy.getDescription().toLowerCase().contains("non-refundable") ||
                   policy.getDescription().toLowerCase().contains("no refund"));
    }

    // FullRefundPolicy Tests

    @Test
    @DisplayName("FullRefundPolicy should always return full amount")
    void testFullRefundPolicy() {
        RefundPolicy policy = new FullRefundPolicy();
        Booking booking = createTestBooking(LocalDate.now().plusDays(1), 500.0);

        double refund = policy.calculateRefund(booking, LocalDate.now());

        assertEquals(500.0, refund, "FullRefundPolicy should return full booking cost");
    }

    @Test
    @DisplayName("FullRefundPolicy should return full amount for any cancellation time")
    void testFullRefundPolicyAnyTime() {
        RefundPolicy policy = new FullRefundPolicy();
        LocalDate checkIn = LocalDate.now().plusDays(1);
        Booking booking = createTestBooking(checkIn, 750.0);

        // Cancel on same day as check-in
        double refund = policy.calculateRefund(booking, checkIn);

        assertEquals(750.0, refund, "FullRefundPolicy should return full amount even on check-in day");
    }

    @Test
    @DisplayName("FullRefundPolicy description should mention full refund")
    void testFullRefundPolicyDescription() {
        RefundPolicy policy = new FullRefundPolicy();

        assertTrue(policy.getDescription().toLowerCase().contains("full") ||
                   policy.getDescription().toLowerCase().contains("100%"));
    }

    // TieredRefundPolicy Tests

    @Test
    @DisplayName("TieredRefundPolicy should give 100% refund if cancelled 7+ days before")
    void testTieredRefundPolicyFullRefund() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate checkIn = LocalDate.now().plusDays(10);
        Booking booking = createTestBooking(checkIn, 500.0);

        double refund = policy.calculateRefund(booking, LocalDate.now());

        assertEquals(500.0, refund, "Should get 100% refund when cancelled 10 days before check-in");
    }

    @Test
    @DisplayName("TieredRefundPolicy should give 100% refund if cancelled exactly 7 days before")
    void testTieredRefundPolicyExactly7Days() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate cancelDate = LocalDate.now();
        LocalDate checkIn = cancelDate.plusDays(7);
        Booking booking = createTestBooking(checkIn, 500.0);

        double refund = policy.calculateRefund(booking, cancelDate);

        assertEquals(500.0, refund, "Should get 100% refund when cancelled exactly 7 days before");
    }

    @Test
    @DisplayName("TieredRefundPolicy should give 50% refund if cancelled 3-6 days before")
    void testTieredRefundPolicyPartialRefund() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate cancelDate = LocalDate.now();
        LocalDate checkIn = cancelDate.plusDays(5); // 5 days before
        Booking booking = createTestBooking(checkIn, 500.0);

        double refund = policy.calculateRefund(booking, cancelDate);

        assertEquals(250.0, refund, "Should get 50% refund when cancelled 5 days before check-in");
    }

    @Test
    @DisplayName("TieredRefundPolicy should give 50% refund if cancelled exactly 3 days before")
    void testTieredRefundPolicyExactly3Days() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate cancelDate = LocalDate.now();
        LocalDate checkIn = cancelDate.plusDays(3);
        Booking booking = createTestBooking(checkIn, 600.0);

        double refund = policy.calculateRefund(booking, cancelDate);

        assertEquals(300.0, refund, "Should get 50% refund when cancelled exactly 3 days before");
    }

    @Test
    @DisplayName("TieredRefundPolicy should give 50% refund if cancelled 6 days before")
    void testTieredRefundPolicy6Days() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate cancelDate = LocalDate.now();
        LocalDate checkIn = cancelDate.plusDays(6);
        Booking booking = createTestBooking(checkIn, 400.0);

        double refund = policy.calculateRefund(booking, cancelDate);

        assertEquals(200.0, refund, "Should get 50% refund when cancelled 6 days before");
    }

    @Test
    @DisplayName("TieredRefundPolicy should give 0% refund if cancelled less than 3 days before")
    void testTieredRefundPolicyNoRefund() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate cancelDate = LocalDate.now();
        LocalDate checkIn = cancelDate.plusDays(2); // 2 days before
        Booking booking = createTestBooking(checkIn, 500.0);

        double refund = policy.calculateRefund(booking, cancelDate);

        assertEquals(0.0, refund, "Should get no refund when cancelled 2 days before check-in");
    }

    @Test
    @DisplayName("TieredRefundPolicy should give 0% refund if cancelled 1 day before")
    void testTieredRefundPolicy1Day() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate cancelDate = LocalDate.now();
        LocalDate checkIn = cancelDate.plusDays(1);
        Booking booking = createTestBooking(checkIn, 500.0);

        double refund = policy.calculateRefund(booking, cancelDate);

        assertEquals(0.0, refund, "Should get no refund when cancelled 1 day before");
    }

    @Test
    @DisplayName("TieredRefundPolicy should give 0% refund if cancelled on check-in day")
    void testTieredRefundPolicySameDay() {
        RefundPolicy policy = new TieredRefundPolicy();
        LocalDate checkIn = LocalDate.now();
        Booking booking = createTestBooking(checkIn, 500.0);

        double refund = policy.calculateRefund(booking, checkIn);

        assertEquals(0.0, refund, "Should get no refund when cancelled on check-in day");
    }

    @Test
    @DisplayName("TieredRefundPolicy description should explain tiers")
    void testTieredRefundPolicyDescription() {
        RefundPolicy policy = new TieredRefundPolicy();
        String description = policy.getDescription();

        assertTrue(description.contains("7") || description.contains("seven"),
            "Description should mention 7-day threshold");
        assertTrue(description.contains("50") || description.contains("half"),
            "Description should mention 50% partial refund");
    }

    // Polymorphism Test

    @Test
    @DisplayName("RefundPolicy interface should support polymorphism")
    void testPolymorphism() {
        RefundPolicy[] policies = {
            new NoRefundPolicy(),
            new FullRefundPolicy(),
            new TieredRefundPolicy()
        };

        LocalDate checkIn = LocalDate.now().plusDays(10);
        Booking booking = createTestBooking(checkIn, 500.0);

        double[] refunds = new double[3];
        for (int i = 0; i < policies.length; i++) {
            refunds[i] = policies[i].calculateRefund(booking, LocalDate.now());
        }

        assertEquals(0.0, refunds[0], "NoRefundPolicy should return 0");
        assertEquals(500.0, refunds[1], "FullRefundPolicy should return full amount");
        assertEquals(500.0, refunds[2], "TieredRefundPolicy should return full amount for 10 days");
    }
}
