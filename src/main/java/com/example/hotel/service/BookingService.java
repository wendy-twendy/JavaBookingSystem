package com.example.hotel.service;

import com.example.hotel.model.Booking;
import com.example.hotel.model.Room;
import com.example.hotel.model.enums.BookingStatus;
import com.example.hotel.model.policy.*;
import com.example.hotel.persistence.FileRepository;
import com.example.hotel.persistence.RepositoryFactory;
import com.example.hotel.persistence.Settings;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing bookings.
 * Contains business logic for booking operations including pricing and refunds.
 */
public class BookingService {

    private final FileRepository<Booking, String> bookingRepository;
    private final RoomService roomService;
    private final Settings settings;

    public BookingService() {
        this.bookingRepository = RepositoryFactory.getInstance().getBookingRepository();
        this.roomService = new RoomService();
        this.settings = Settings.getInstance();
    }

    // Constructor for testing
    public BookingService(FileRepository<Booking, String> bookingRepository,
                          RoomService roomService, Settings settings) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
        this.settings = settings;
    }

    /**
     * Get all bookings.
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Find a booking by ID.
     */
    public Optional<Booking> findById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }

    /**
     * Create a new booking.
     * @throws IllegalArgumentException if validation fails
     */
    public Booking createBooking(String guestId, String roomNumber,
                                  LocalDate checkIn, LocalDate checkOut) {
        // Validate dates
        validateBookingDates(checkIn, checkOut);

        // Check room exists and is available
        Room room = roomService.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Room " + roomNumber + " not found"));

        if (!room.isAvailable()) {
            throw new IllegalArgumentException(
                "Room " + roomNumber + " is not available");
        }

        // Check for overlapping bookings
        if (hasOverlappingBooking(roomNumber, checkIn, checkOut)) {
            throw new IllegalArgumentException(
                "Room " + roomNumber + " has conflicting bookings for these dates");
        }

        // Calculate cost
        double totalCost = calculateTotalCost(room, checkIn, checkOut);

        // Create booking
        Booking booking = new Booking(
            generateBookingId(),
            guestId,
            roomNumber,
            checkIn,
            checkOut,
            BookingStatus.CONFIRMED,
            totalCost
        );

        // Save booking and update room availability
        bookingRepository.save(booking);
        roomService.setAvailability(roomNumber, false);

        return booking;
    }

    /**
     * Cancel a booking and calculate refund.
     * @return The updated booking with refund amount
     */
    public Booking cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Booking " + bookingId + " not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed booking");
        }

        // Get room to determine refund policy
        Room room = roomService.findByRoomNumber(booking.getRoomNumber())
                .orElse(null);

        // Calculate refund
        RefundPolicy policy = getRefundPolicy(room);
        double refundAmount = policy.calculateRefund(booking, LocalDate.now());

        // Update booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setRefundAmount(refundAmount);
        bookingRepository.save(booking);

        // Free up the room
        if (room != null) {
            roomService.setAvailability(room.getRoomNumber(), true);
        }

        return booking;
    }

    /**
     * Complete a booking (guest checked out).
     */
    public Booking completeBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Booking " + bookingId + " not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                "Only confirmed bookings can be completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        // Free up the room
        roomService.setAvailability(booking.getRoomNumber(), true);

        return booking;
    }

    /**
     * Get bookings for a specific guest.
     */
    public List<Booking> getBookingsByGuest(String guestId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getGuestId().equals(guestId))
                .collect(Collectors.toList());
    }

    /**
     * Get bookings for a specific room.
     */
    public List<Booking> getBookingsByRoom(String roomNumber) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getRoomNumber().equals(roomNumber))
                .collect(Collectors.toList());
    }

    /**
     * Get active (confirmed) bookings.
     */
    public List<Booking> getActiveBookings() {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());
    }

    /**
     * Get count of active bookings.
     */
    public long getActiveBookingCount() {
        return getActiveBookings().size();
    }

    /**
     * Calculate the total cost for a booking.
     */
    public double calculateTotalCost(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double subtotal = nights * room.getPricePerNight();
        double vat = subtotal * settings.getVatRate();
        return subtotal + vat;
    }

    /**
     * Calculate the subtotal (before VAT).
     */
    public double calculateSubtotal(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return nights * room.getPricePerNight();
    }

    /**
     * Validate booking dates.
     */
    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
    }

    /**
     * Check if there's an overlapping booking for the room.
     */
    private boolean hasOverlappingBooking(String roomNumber,
                                          LocalDate checkIn, LocalDate checkOut) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getRoomNumber().equals(roomNumber))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .anyMatch(b -> datesOverlap(
                    b.getCheckInDate(), b.getCheckOutDate(), checkIn, checkOut));
    }

    /**
     * Check if two date ranges overlap.
     */
    private boolean datesOverlap(LocalDate start1, LocalDate end1,
                                 LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Get the appropriate refund policy for a room.
     */
    private RefundPolicy getRefundPolicy(Room room) {
        if (room == null || !room.isRefundable()) {
            return new NoRefundPolicy();
        }

        String policyType = settings.getDefaultRefundPolicy();
        switch (policyType.toUpperCase()) {
            case "FULL":
                return new FullRefundPolicy();
            case "NONE":
                return new NoRefundPolicy();
            case "TIERED":
            default:
                return new TieredRefundPolicy();
        }
    }

    /**
     * Generate a unique booking ID.
     */
    private String generateBookingId() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
