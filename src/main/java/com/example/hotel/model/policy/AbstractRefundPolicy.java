package com.example.hotel.model.policy;

/**
 * Abstract base class for refund policies.
 * Provides shared functionality (policy name, toString) while leaving
 * calculateRefund() and getDescription() to concrete implementations.
 *
 * Demonstrates: Interface -> Abstract Class -> Concrete Class hierarchy.
 */
public abstract class AbstractRefundPolicy implements RefundPolicy {

    private final String policyName;

    protected AbstractRefundPolicy(String policyName) {
        this.policyName = policyName;
    }

    /**
     * Get the display name of this policy.
     */
    public String getPolicyName() {
        return policyName;
    }

    @Override
    public String toString() {
        return policyName + ": " + getDescription();
    }
}
