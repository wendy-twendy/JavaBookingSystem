package com.example.hotel.model;

import com.example.hotel.model.enums.BookingStatus;
import com.example.hotel.model.enums.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AbstractEntity base class.
 * Verifies that equals/hashCode work correctly through the abstract class
 * for all entity types: Room, Guest, Booking, Invoice.
 */
class AbstractEntityTest {

    // Room tests

    @Test
    @DisplayName("Rooms with same room number should be equal")
    void testRoomEqualsSameId() {
        Room room1 = new Room("101", RoomType.SINGLE, 100.0, true, true);
        Room room2 = new Room("101", RoomType.DOUBLE, 200.0, false, false);

        assertEquals(room1, room2, "Rooms with same room number should be equal");
    }

    @Test
    @DisplayName("Rooms with different room numbers should not be equal")
    void testRoomEqualsDifferentId() {
        Room room1 = new Room("101", RoomType.SINGLE, 100.0, true, true);
        Room room2 = new Room("102", RoomType.SINGLE, 100.0, true, true);

        assertNotEquals(room1, room2, "Rooms with different room numbers should not be equal");
    }

    @Test
    @DisplayName("Room getId() should return room number")
    void testRoomGetId() {
        Room room = new Room("101", RoomType.SINGLE, 100.0, true, true);

        assertEquals("101", room.getId());
    }

    // Guest tests

    @Test
    @DisplayName("Guests with same ID should be equal")
    void testGuestEqualsSameId() {
        Guest guest1 = new Guest("G-001", "Alice", "123", "alice@test.com");
        Guest guest2 = new Guest("G-001", "Bob", "456", "bob@test.com");

        assertEquals(guest1, guest2, "Guests with same ID should be equal");
    }

    @Test
    @DisplayName("Guests with different IDs should not be equal")
    void testGuestEqualsDifferentId() {
        Guest guest1 = new Guest("G-001", "Alice", "123", "alice@test.com");
        Guest guest2 = new Guest("G-002", "Alice", "123", "alice@test.com");

        assertNotEquals(guest1, guest2, "Guests with different IDs should not be equal");
    }

    @Test
    @DisplayName("Guest getId() should return guest ID")
    void testGuestGetId() {
        Guest guest = new Guest("G-001", "Alice", "123", "alice@test.com");

        assertEquals("G-001", guest.getId());
    }

    // Booking tests

    @Test
    @DisplayName("Bookings with same ID should be equal")
    void testBookingEqualsSameId() {
        Booking booking1 = new Booking("BK-001", "G-001", "101",
                LocalDate.now(), LocalDate.now().plusDays(2), BookingStatus.CONFIRMED, 200.0);
        Booking booking2 = new Booking("BK-001", "G-002", "102",
                LocalDate.now(), LocalDate.now().plusDays(5), BookingStatus.CANCELLED, 500.0);

        assertEquals(booking1, booking2, "Bookings with same ID should be equal");
    }

    @Test
    @DisplayName("Bookings with different IDs should not be equal")
    void testBookingEqualsDifferentId() {
        Booking booking1 = new Booking("BK-001", "G-001", "101",
                LocalDate.now(), LocalDate.now().plusDays(2), BookingStatus.CONFIRMED, 200.0);
        Booking booking2 = new Booking("BK-002", "G-001", "101",
                LocalDate.now(), LocalDate.now().plusDays(2), BookingStatus.CONFIRMED, 200.0);

        assertNotEquals(booking1, booking2, "Bookings with different IDs should not be equal");
    }

    @Test
    @DisplayName("Booking getId() should return booking ID")
    void testBookingGetId() {
        Booking booking = new Booking("BK-001", "G-001", "101",
                LocalDate.now(), LocalDate.now().plusDays(2), BookingStatus.CONFIRMED, 200.0);

        assertEquals("BK-001", booking.getId());
    }

    // Invoice tests

    @Test
    @DisplayName("Invoices with same ID should be equal")
    void testInvoiceEqualsSameId() {
        Invoice inv1 = new Invoice("INV-001", "BK-001", 100.0, 0.10, 10.0, 110.0);
        Invoice inv2 = new Invoice("INV-001", "BK-002", 200.0, 0.10, 20.0, 220.0);

        assertEquals(inv1, inv2, "Invoices with same ID should be equal");
    }

    @Test
    @DisplayName("Invoices with different IDs should not be equal")
    void testInvoiceEqualsDifferentId() {
        Invoice inv1 = new Invoice("INV-001", "BK-001", 100.0, 0.10, 10.0, 110.0);
        Invoice inv2 = new Invoice("INV-002", "BK-001", 100.0, 0.10, 10.0, 110.0);

        assertNotEquals(inv1, inv2, "Invoices with different IDs should not be equal");
    }

    @Test
    @DisplayName("Invoice getId() should return invoice ID")
    void testInvoiceGetId() {
        Invoice invoice = new Invoice("INV-001", "BK-001", 100.0, 0.10, 10.0, 110.0);

        assertEquals("INV-001", invoice.getId());
    }

    // Cross-type and edge case tests

    @Test
    @DisplayName("Different entity types should not be equal even with same ID string")
    void testDifferentTypesNotEqual() {
        Room room = new Room("101", RoomType.SINGLE, 100.0, true, true);
        Guest guest = new Guest("101", "Alice", "123", "alice@test.com");

        assertNotEquals(room, guest, "Different entity types should not be equal");
    }

    @Test
    @DisplayName("Entity should not be equal to null")
    void testNotEqualToNull() {
        Room room = new Room("101", RoomType.SINGLE, 100.0, true, true);

        assertNotEquals(null, room);
    }

    @Test
    @DisplayName("Entity should be equal to itself")
    void testEqualToSelf() {
        Room room = new Room("101", RoomType.SINGLE, 100.0, true, true);

        assertEquals(room, room, "Entity should be equal to itself");
    }

    @Test
    @DisplayName("Equal entities should have same hash code")
    void testHashCodeConsistency() {
        Room room1 = new Room("101", RoomType.SINGLE, 100.0, true, true);
        Room room2 = new Room("101", RoomType.DOUBLE, 200.0, false, false);

        assertEquals(room1.hashCode(), room2.hashCode(),
                "Equal entities should have the same hash code");
    }

    @Test
    @DisplayName("Entity with null ID should handle equals correctly")
    void testNullIdEquals() {
        Room room1 = new Room();
        Room room2 = new Room();

        // Both have null IDs - should be equal per Objects.equals(null, null)
        assertEquals(room1, room2, "Entities with null IDs should be equal");
    }

    @Test
    @DisplayName("Entity with null ID vs non-null ID should not be equal")
    void testNullVsNonNullId() {
        Room room1 = new Room();
        Room room2 = new Room("101", RoomType.SINGLE, 100.0, true, true);

        assertNotEquals(room1, room2, "Entity with null ID should not equal entity with non-null ID");
    }
}
