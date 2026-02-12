package com.example.hotel.model.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AbstractRefundPolicy base class.
 * Tests getPolicyName() and toString() for each policy implementation.
 */
class AbstractRefundPolicyTest {

    @Test
    @DisplayName("NoRefundPolicy should have correct policy name")
    void testNoRefundPolicyName() {
        NoRefundPolicy policy = new NoRefundPolicy();

        assertEquals("No Refund", policy.getPolicyName());
    }

    @Test
    @DisplayName("FullRefundPolicy should have correct policy name")
    void testFullRefundPolicyName() {
        FullRefundPolicy policy = new FullRefundPolicy();

        assertEquals("Full Refund", policy.getPolicyName());
    }

    @Test
    @DisplayName("TieredRefundPolicy should have correct policy name")
    void testTieredRefundPolicyName() {
        TieredRefundPolicy policy = new TieredRefundPolicy();

        assertEquals("Tiered Refund", policy.getPolicyName());
    }

    @Test
    @DisplayName("NoRefundPolicy toString should include name and description")
    void testNoRefundPolicyToString() {
        NoRefundPolicy policy = new NoRefundPolicy();
        String result = policy.toString();

        assertTrue(result.startsWith("No Refund: "),
                "toString should start with policy name");
        assertTrue(result.contains(policy.getDescription()),
                "toString should include description");
    }

    @Test
    @DisplayName("FullRefundPolicy toString should include name and description")
    void testFullRefundPolicyToString() {
        FullRefundPolicy policy = new FullRefundPolicy();
        String result = policy.toString();

        assertTrue(result.startsWith("Full Refund: "),
                "toString should start with policy name");
        assertTrue(result.contains(policy.getDescription()),
                "toString should include description");
    }

    @Test
    @DisplayName("TieredRefundPolicy toString should include name and description")
    void testTieredRefundPolicyToString() {
        TieredRefundPolicy policy = new TieredRefundPolicy();
        String result = policy.toString();

        assertTrue(result.startsWith("Tiered Refund: "),
                "toString should start with policy name");
        assertTrue(result.contains(policy.getDescription()),
                "toString should include description");
    }

    @Test
    @DisplayName("All policies should be instances of AbstractRefundPolicy")
    void testInstanceOfAbstractRefundPolicy() {
        assertTrue(new NoRefundPolicy() instanceof AbstractRefundPolicy);
        assertTrue(new FullRefundPolicy() instanceof AbstractRefundPolicy);
        assertTrue(new TieredRefundPolicy() instanceof AbstractRefundPolicy);
    }

    @Test
    @DisplayName("All policies should also be instances of RefundPolicy interface")
    void testInstanceOfRefundPolicy() {
        assertTrue(new NoRefundPolicy() instanceof RefundPolicy);
        assertTrue(new FullRefundPolicy() instanceof RefundPolicy);
        assertTrue(new TieredRefundPolicy() instanceof RefundPolicy);
    }
}
