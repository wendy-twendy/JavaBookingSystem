package com.example.hotel.service;

import com.example.hotel.model.Booking;
import com.example.hotel.model.Guest;
import com.example.hotel.model.Room;
import com.example.hotel.model.enums.BookingStatus;
import com.example.hotel.model.enums.RoomType;
import com.example.hotel.persistence.FileRepository;
import com.example.hotel.persistence.RepositoryFactory;
import com.example.hotel.persistence.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookingService.
 */
class BookingServiceTest {

    private BookingService bookingService;
    private RoomService roomService;
    private GuestService guestService;
    private FileRepository<Booking, String> bookingRepository;
    private FileRepository<Room, String> roomRepository;
    private FileRepository<Guest, String> guestRepository;

    private static final String TEST_ROOM = "TEST-B101";
    private static final String TEST_GUEST = "TEST-BG001";

    @BeforeEach
    void setUp() {
        RepositoryFactory.getInstance().refreshAll();
        bookingRepository = RepositoryFactory.getInstance().getBookingRepository();
        roomRepository = RepositoryFactory.getInstance().getRoomRepository();
        guestRepository = RepositoryFactory.getInstance().getGuestRepository();

        roomService = new RoomService(roomRepository);
        guestService = new GuestService(guestRepository);
        bookingService = new BookingService(bookingRepository, roomService, Settings.getInstance());

        // Set up test data
        Room testRoom = new Room(TEST_ROOM, RoomType.DOUBLE, 150.0, true, true);
        roomRepository.save(testRoom);

        Guest testGuest = new Guest(TEST_GUEST, "Test Guest", "5551234567", "testguest@example.com");
        guestRepository.save(testGuest);
    }

    @AfterEach
    void tearDown() {
        // Clean up test bookings
        List<Booking> allBookings = bookingRepository.findAll();
        for (Booking booking : allBookings) {
            if (booking.getRoomNumber().startsWith("TEST-") ||
                booking.getGuestId().startsWith("TEST-")) {
                bookingRepository.delete(booking.getBookingId());
            }
        }

        // Clean up test room and guest
        roomRepository.delete(TEST_ROOM);
        guestRepository.delete(TEST_GUEST);
    }

    @Test
    @DisplayName("Should create booking successfully")
    void testCreateBooking() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);

        assertNotNull(booking.getBookingId(), "Booking ID should be generated");
        assertTrue(booking.getBookingId().startsWith("BK-"), "Booking ID should start with 'BK-'");
        assertEquals(TEST_GUEST, booking.getGuestId());
        assertEquals(TEST_ROOM, booking.getRoomNumber());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(checkIn, booking.getCheckInDate());
        assertEquals(checkOut, booking.getCheckOutDate());
        assertTrue(booking.getTotalCost() > 0, "Total cost should be calculated");

        // Room should be marked unavailable
        Optional<Room> room = roomService.findByRoomNumber(TEST_ROOM);
        assertTrue(room.isPresent());
        assertFalse(room.get().isAvailable(), "Room should be unavailable after booking");
    }

    @Test
    @DisplayName("Should reject booking for unavailable room")
    void testBookingUnavailableRoom() {
        // Make room unavailable
        roomService.setAvailability(TEST_ROOM, false);

        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut)
        );
        assertTrue(exception.getMessage().contains("not available"));
    }

    @Test
    @DisplayName("Should reject booking for non-existent room")
    void testBookingNonExistentRoom() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(TEST_GUEST, "NONEXISTENT", checkIn, checkOut)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should reject booking with past check-in date")
    void testBookingPastDate() {
        LocalDate checkIn = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut)
        );
        assertTrue(exception.getMessage().contains("past"));
    }

    @Test
    @DisplayName("Should reject booking with invalid date range")
    void testBookingInvalidDateRange() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(3); // Check-out before check-in

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut)
        );
        assertTrue(exception.getMessage().contains("after"));
    }

    @Test
    @DisplayName("Should reject booking with same check-in and check-out date")
    void testBookingSameDateRange() {
        LocalDate sameDate = LocalDate.now().plusDays(5);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(TEST_GUEST, TEST_ROOM, sameDate, sameDate)
        );
        assertTrue(exception.getMessage().contains("after"));
    }

    @Test
    @DisplayName("Should reject booking with null dates")
    void testBookingNullDates() {
        LocalDate checkIn = LocalDate.now().plusDays(1);

        assertThrows(IllegalArgumentException.class,
            () -> bookingService.createBooking(TEST_GUEST, TEST_ROOM, null, checkIn));

        assertThrows(IllegalArgumentException.class,
            () -> bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, null));
    }

    @Test
    @DisplayName("Should cancel booking and calculate refund")
    void testCancelBooking() {
        // Create a booking far in the future to ensure full refund
        LocalDate checkIn = LocalDate.now().plusDays(14);
        LocalDate checkOut = LocalDate.now().plusDays(16);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);
        String bookingId = booking.getBookingId();
        double originalCost = booking.getTotalCost();

        Booking cancelledBooking = bookingService.cancelBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());
        assertTrue(cancelledBooking.getRefundAmount() > 0, "Refund should be calculated");
        assertEquals(originalCost, cancelledBooking.getRefundAmount(), 0.01,
            "Should receive full refund when cancelled 14 days before");

        // Room should be available again
        Optional<Room> room = roomService.findByRoomNumber(TEST_ROOM);
        assertTrue(room.isPresent());
        assertTrue(room.get().isAvailable(), "Room should be available after cancellation");
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled booking")
    void testCancelAlreadyCancelledBooking() {
        LocalDate checkIn = LocalDate.now().plusDays(14);
        LocalDate checkOut = LocalDate.now().plusDays(16);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);
        bookingService.cancelBooking(booking.getBookingId());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.cancelBooking(booking.getBookingId())
        );
        assertTrue(exception.getMessage().contains("already cancelled"));
    }

    @Test
    @DisplayName("Should complete booking successfully")
    void testCompleteBooking() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);

        Booking completedBooking = bookingService.completeBooking(booking.getBookingId());

        assertEquals(BookingStatus.COMPLETED, completedBooking.getStatus());

        // Room should be available again
        Optional<Room> room = roomService.findByRoomNumber(TEST_ROOM);
        assertTrue(room.isPresent());
        assertTrue(room.get().isAvailable(), "Room should be available after completion");
    }

    @Test
    @DisplayName("Should throw exception when completing non-confirmed booking")
    void testCompleteNonConfirmedBooking() {
        LocalDate checkIn = LocalDate.now().plusDays(14);
        LocalDate checkOut = LocalDate.now().plusDays(16);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);
        bookingService.cancelBooking(booking.getBookingId());

        // Reset room availability for this test
        roomService.setAvailability(TEST_ROOM, true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.completeBooking(booking.getBookingId())
        );
        assertTrue(exception.getMessage().contains("confirmed"));
    }

    @Test
    @DisplayName("Should calculate cost correctly with VAT")
    void testCalculateCost() {
        Room room = new Room("CALC-TEST", RoomType.SINGLE, 100.0, true, true);
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(4); // 3 nights

        double subtotal = bookingService.calculateSubtotal(room, checkIn, checkOut);
        double total = bookingService.calculateTotalCost(room, checkIn, checkOut);

        assertEquals(300.0, subtotal, 0.01, "Subtotal should be 3 nights * $100");

        // VAT rate from settings is 0.20 (20%)
        double expectedTotal = 300.0 * 1.20; // 360.0
        assertEquals(expectedTotal, total, 0.01, "Total should include 20% VAT");
    }

    @Test
    @DisplayName("Should return bookings for specific guest")
    void testGetBookingsByGuest() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);

        List<Booking> guestBookings = bookingService.getBookingsByGuest(TEST_GUEST);

        assertTrue(guestBookings.stream()
            .anyMatch(b -> b.getBookingId().equals(booking.getBookingId())));
    }

    @Test
    @DisplayName("Should return bookings for specific room")
    void testGetBookingsByRoom() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);

        List<Booking> roomBookings = bookingService.getBookingsByRoom(TEST_ROOM);

        assertTrue(roomBookings.stream()
            .anyMatch(b -> b.getBookingId().equals(booking.getBookingId())));
    }

    @Test
    @DisplayName("Should return active bookings")
    void testGetActiveBookings() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Booking booking = bookingService.createBooking(TEST_GUEST, TEST_ROOM, checkIn, checkOut);

        List<Booking> activeBookings = bookingService.getActiveBookings();

        assertTrue(activeBookings.stream()
            .anyMatch(b -> b.getBookingId().equals(booking.getBookingId())),
            "Confirmed booking should be in active list");

        // Cancel and verify it's no longer active
        bookingService.cancelBooking(booking.getBookingId());

        activeBookings = bookingService.getActiveBookings();
        assertFalse(activeBookings.stream()
            .anyMatch(b -> b.getBookingId().equals(booking.getBookingId())),
            "Cancelled booking should not be in active list");
    }
}
