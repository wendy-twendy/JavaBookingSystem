package com.example.hotel.persistence;

import com.example.hotel.model.Booking;
import com.example.hotel.model.Guest;
import com.example.hotel.model.Invoice;
import com.example.hotel.model.Room;
import com.example.hotel.model.enums.BookingStatus;
import com.example.hotel.model.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the persistence layer.
 */
class PersistenceTest {

    @BeforeEach
    void setUp() {
        // Refresh all repositories before each test
        RepositoryFactory.getInstance().refreshAll();
    }

    @Test
    void testJsonUtilsDateSerialization() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        String json = JsonUtils.toJson(date);
        assertTrue(json.contains("2024-06-15"));

        LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
        String jsonDateTime = JsonUtils.toJson(dateTime);
        assertTrue(jsonDateTime.contains("2024-06-15T10:30:00"));
    }

    @Test
    void testRoomRepositoryLoadsFromFile() {
        FileRepository<Room, String> repo = RepositoryFactory.getInstance().getRoomRepository();
        List<Room> rooms = repo.findAll();

        assertFalse(rooms.isEmpty(), "Should load rooms from file");

        Optional<Room> room101 = repo.findById("101");
        assertTrue(room101.isPresent(), "Should find room 101");
        assertEquals(RoomType.SINGLE, room101.get().getType());
        assertEquals(8000.0, room101.get().getPricePerNight());
    }

    @Test
    void testRoomRepositorySaveAndDelete() {
        FileRepository<Room, String> repo = RepositoryFactory.getInstance().getRoomRepository();

        Room newRoom = new Room("999", RoomType.SUITE, 500.0, true, true);
        repo.save(newRoom);

        assertTrue(repo.existsById("999"));
        assertEquals(repo.findById("999").get().getPricePerNight(), 500.0);

        // Update
        newRoom.setPricePerNight(550.0);
        repo.save(newRoom);
        assertEquals(550.0, repo.findById("999").get().getPricePerNight());

        // Delete
        assertTrue(repo.delete("999"));
        assertFalse(repo.existsById("999"));
    }

    @Test
    void testGuestRepository() {
        FileRepository<Guest, String> repo = RepositoryFactory.getInstance().getGuestRepository();

        Guest guest = new Guest("G001", "John Doe", "555-1234", "john@example.com");
        repo.save(guest);

        assertTrue(repo.existsById("G001"));
        assertEquals("John Doe", repo.findById("G001").get().getName());

        // Cleanup
        repo.delete("G001");
    }

    @Test
    void testBookingRepositoryWithDates() {
        FileRepository<Booking, String> repo = RepositoryFactory.getInstance().getBookingRepository();

        Booking booking = new Booking(
                "B001", "G001", "101",
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 7, 5),
                BookingStatus.CONFIRMED, 440.0
        );
        repo.save(booking);

        // Reload from file to verify date serialization
        repo.refresh();

        Optional<Booking> loaded = repo.findById("B001");
        assertTrue(loaded.isPresent());
        assertEquals(LocalDate.of(2024, 7, 1), loaded.get().getCheckInDate());
        assertEquals(LocalDate.of(2024, 7, 5), loaded.get().getCheckOutDate());
        assertEquals(4, loaded.get().getNumberOfNights());

        // Cleanup
        repo.delete("B001");
    }

    @Test
    void testInvoiceRepositoryWithDateTime() {
        FileRepository<Invoice, String> repo = RepositoryFactory.getInstance().getInvoiceRepository();

        Invoice invoice = new Invoice("INV001", "B001", 400.0, 0.10, 40.0, 440.0);
        repo.save(invoice);

        // Reload to verify datetime serialization
        repo.refresh();

        Optional<Invoice> loaded = repo.findById("INV001");
        assertTrue(loaded.isPresent());
        assertNotNull(loaded.get().getGeneratedAt());
        assertEquals(440.0, loaded.get().getTotal());

        // Cleanup
        repo.delete("INV001");
    }

    @Test
    void testSettingsSingleton() {
        Settings settings = Settings.getInstance();

        assertEquals(0.20, settings.getVatRate());
        assertEquals("ALL", settings.getCurrency());
        assertEquals("Hotel Tirana", settings.getHotelName());
        assertEquals("TIERED", settings.getDefaultRefundPolicy());
    }

    @Test
    void testRepositoryCount() {
        FileRepository<Room, String> repo = RepositoryFactory.getInstance().getRoomRepository();
        long count = repo.count();
        assertTrue(count >= 2, "Should have at least 2 rooms from test data");
    }
}
