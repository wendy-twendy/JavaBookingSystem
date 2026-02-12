package com.example.hotel.model;

import java.util.Objects;

/**
 * Abstract base class for all entity models.
 * Provides common identity-based equals() and hashCode() implementations,
 * demonstrating the use of abstract classes in OOP.
 */
public abstract class AbstractEntity {

    /**
     * Get the unique identifier for this entity.
     * Each subclass defines its own ID field (roomNumber, id, bookingId, invoiceId).
     */
    public abstract String getId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEntity that = (AbstractEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
