package com.example.hotel.service;

import com.example.hotel.model.Room;
import com.example.hotel.model.enums.RoomType;
import com.example.hotel.persistence.FileRepository;
import com.example.hotel.persistence.RepositoryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoomService.
 */
class RoomServiceTest {

    private RoomService roomService;
    private FileRepository<Room, String> roomRepository;

    @BeforeEach
    void setUp() {
        RepositoryFactory.getInstance().refreshAll();
        roomRepository = RepositoryFactory.getInstance().getRoomRepository();
        roomService = new RoomService(roomRepository);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        roomRepository.delete("TEST-101");
        roomRepository.delete("TEST-102");
        roomRepository.delete("TEST-103");
    }

    @Test
    @DisplayName("Should add room successfully")
    void testAddRoom() {
        Room room = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);

        roomService.addRoom(room);

        Optional<Room> found = roomService.findByRoomNumber("TEST-101");
        assertTrue(found.isPresent(), "Room should be found after adding");
        assertEquals(RoomType.SINGLE, found.get().getType());
        assertEquals(100.0, found.get().getPricePerNight());
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate room")
    void testAddDuplicateRoom() {
        Room room1 = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);
        Room room2 = new Room("TEST-101", RoomType.DOUBLE, 150.0, true, true);

        roomService.addRoom(room1);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roomService.addRoom(room2)
        );
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should update existing room")
    void testUpdateRoom() {
        Room room = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);
        roomService.addRoom(room);

        room.setPricePerNight(120.0);
        room.setType(RoomType.DOUBLE);
        roomService.updateRoom(room);

        Optional<Room> updated = roomService.findByRoomNumber("TEST-101");
        assertTrue(updated.isPresent());
        assertEquals(120.0, updated.get().getPricePerNight());
        assertEquals(RoomType.DOUBLE, updated.get().getType());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent room")
    void testUpdateNonExistentRoom() {
        Room room = new Room("TEST-NONEXISTENT", RoomType.SINGLE, 100.0, true, true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roomService.updateRoom(room)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should delete room successfully")
    void testDeleteRoom() {
        Room room = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);
        roomService.addRoom(room);

        boolean deleted = roomService.deleteRoom("TEST-101");

        assertTrue(deleted, "Delete should return true for existing room");
        assertFalse(roomService.findByRoomNumber("TEST-101").isPresent(),
            "Room should not be found after deletion");
    }

    @Test
    @DisplayName("Should return false when deleting non-existent room")
    void testDeleteNonExistentRoom() {
        boolean deleted = roomService.deleteRoom("TEST-NONEXISTENT");
        assertFalse(deleted, "Delete should return false for non-existent room");
    }

    @Test
    @DisplayName("Should return only available rooms")
    void testGetAvailableRooms() {
        Room availableRoom = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);
        Room unavailableRoom = new Room("TEST-102", RoomType.DOUBLE, 150.0, false, true);
        roomService.addRoom(availableRoom);
        roomService.addRoom(unavailableRoom);

        List<Room> availableRooms = roomService.getAvailableRooms();

        assertTrue(availableRooms.stream()
            .anyMatch(r -> r.getRoomNumber().equals("TEST-101")),
            "Available room should be in the list");
        assertFalse(availableRooms.stream()
            .anyMatch(r -> r.getRoomNumber().equals("TEST-102")),
            "Unavailable room should not be in the list");
    }

    @Test
    @DisplayName("Should search rooms by partial room number")
    void testSearchByRoomNumber() {
        Room room1 = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);
        Room room2 = new Room("TEST-102", RoomType.DOUBLE, 150.0, true, true);
        roomService.addRoom(room1);
        roomService.addRoom(room2);

        List<Room> results = roomService.searchByRoomNumber("TEST-10");

        assertEquals(2, results.size(), "Should find both rooms with partial match");

        List<Room> specificResults = roomService.searchByRoomNumber("TEST-101");
        assertEquals(1, specificResults.size(), "Should find exact room");
    }

    @Test
    @DisplayName("Should search rooms case-insensitively")
    void testSearchByRoomNumberCaseInsensitive() {
        Room room = new Room("TEST-ABC", RoomType.SINGLE, 100.0, true, true);
        roomService.addRoom(room);

        List<Room> results = roomService.searchByRoomNumber("test-abc");

        assertEquals(1, results.size(), "Search should be case-insensitive");

        // Clean up
        roomRepository.delete("TEST-ABC");
    }

    @Test
    @DisplayName("Should toggle room availability")
    void testSetAvailability() {
        Room room = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);
        roomService.addRoom(room);

        roomService.setAvailability("TEST-101", false);

        Optional<Room> updated = roomService.findByRoomNumber("TEST-101");
        assertTrue(updated.isPresent());
        assertFalse(updated.get().isAvailable(), "Room should be unavailable");

        roomService.setAvailability("TEST-101", true);

        updated = roomService.findByRoomNumber("TEST-101");
        assertTrue(updated.get().isAvailable(), "Room should be available again");
    }

    @Test
    @DisplayName("Should throw exception when setting availability for non-existent room")
    void testSetAvailabilityNonExistentRoom() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roomService.setAvailability("TEST-NONEXISTENT", false)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should return available rooms by type")
    void testGetAvailableRoomsByType() {
        Room single = new Room("TEST-101", RoomType.SINGLE, 100.0, true, true);
        Room double1 = new Room("TEST-102", RoomType.DOUBLE, 150.0, true, true);
        Room double2 = new Room("TEST-103", RoomType.DOUBLE, 150.0, false, true);
        roomService.addRoom(single);
        roomService.addRoom(double1);
        roomService.addRoom(double2);

        List<Room> singleRooms = roomService.getAvailableRoomsByType(RoomType.SINGLE);
        List<Room> doubleRooms = roomService.getAvailableRoomsByType(RoomType.DOUBLE);

        assertTrue(singleRooms.stream()
            .anyMatch(r -> r.getRoomNumber().equals("TEST-101")));
        assertTrue(doubleRooms.stream()
            .anyMatch(r -> r.getRoomNumber().equals("TEST-102")));
        assertFalse(doubleRooms.stream()
            .anyMatch(r -> r.getRoomNumber().equals("TEST-103")),
            "Unavailable double room should not be included");
    }
}
