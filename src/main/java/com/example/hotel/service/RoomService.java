package com.example.hotel.service;

import com.example.hotel.model.Room;
import com.example.hotel.model.enums.RoomType;
import com.example.hotel.persistence.FileRepository;
import com.example.hotel.persistence.RepositoryFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing rooms.
 * Contains business logic for room operations.
 */
public class RoomService extends AbstractService<Room> {

    public RoomService() {
        super(RepositoryFactory.getInstance().getRoomRepository());
    }

    // Constructor for testing with mock repository
    public RoomService(FileRepository<Room, String> roomRepository) {
        super(roomRepository);
    }

    /**
     * Get all rooms.
     */
    public List<Room> getAllRooms() {
        return getAll();
    }

    /**
     * Find a room by room number.
     */
    public Optional<Room> findByRoomNumber(String roomNumber) {
        return findById(roomNumber);
    }

    /**
     * Add a new room.
     * @throws IllegalArgumentException if room number already exists
     */
    public void addRoom(Room room) {
        if (repository.existsById(room.getRoomNumber())) {
            throw new IllegalArgumentException(
                "Room with number " + room.getRoomNumber() + " already exists");
        }
        repository.save(room);
    }

    /**
     * Update an existing room.
     * @throws IllegalArgumentException if room doesn't exist
     */
    public void updateRoom(Room room) {
        if (!repository.existsById(room.getRoomNumber())) {
            throw new IllegalArgumentException(
                "Room with number " + room.getRoomNumber() + " not found");
        }
        repository.save(room);
    }

    /**
     * Delete a room by room number.
     * @return true if deleted, false if not found
     */
    public boolean deleteRoom(String roomNumber) {
        return delete(roomNumber);
    }

    /**
     * Get all available rooms.
     */
    public List<Room> getAvailableRooms() {
        return repository.findAll().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Get available rooms of a specific type.
     */
    public List<Room> getAvailableRoomsByType(RoomType type) {
        return repository.findAll().stream()
                .filter(Room::isAvailable)
                .filter(room -> room.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Search rooms by room number (partial match).
     */
    public List<Room> searchByRoomNumber(String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase();
        return repository.findAll().stream()
                .filter(room -> room.getRoomNumber().toLowerCase().contains(lowerSearch))
                .collect(Collectors.toList());
    }

    /**
     * Set room availability.
     */
    public void setAvailability(String roomNumber, boolean available) {
        Room room = findById(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Room " + roomNumber + " not found"));
        room.setAvailable(available);
        repository.save(room);
    }

    /**
     * Get total room count.
     */
    public long getTotalRoomCount() {
        return count();
    }

    /**
     * Get available room count.
     */
    public long getAvailableRoomCount() {
        return repository.findAll().stream()
                .filter(Room::isAvailable)
                .count();
    }

    /**
     * Get rooms that are available AND have no overlapping bookings for the specified dates.
     * Used by the Guest Portal for browsing available rooms.
     */
    public List<Room> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut,
                                                 BookingService bookingService) {
        return repository.findAll().stream()
                .filter(Room::isAvailable)
                .filter(room -> bookingService.isRoomAvailableForDates(
                    room.getRoomNumber(), checkIn, checkOut))
                .collect(Collectors.toList());
    }
}
