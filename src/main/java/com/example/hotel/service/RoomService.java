package com.example.hotel.service;

import com.example.hotel.model.Room;
import com.example.hotel.model.enums.RoomType;
import com.example.hotel.persistence.FileRepository;
import com.example.hotel.persistence.RepositoryFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing rooms.
 * Contains business logic for room operations.
 */
public class RoomService {

    private final FileRepository<Room, String> roomRepository;

    public RoomService() {
        this.roomRepository = RepositoryFactory.getInstance().getRoomRepository();
    }

    // Constructor for testing with mock repository
    public RoomService(FileRepository<Room, String> roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Get all rooms.
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Find a room by room number.
     */
    public Optional<Room> findByRoomNumber(String roomNumber) {
        return roomRepository.findById(roomNumber);
    }

    /**
     * Add a new room.
     * @throws IllegalArgumentException if room number already exists
     */
    public void addRoom(Room room) {
        if (roomRepository.existsById(room.getRoomNumber())) {
            throw new IllegalArgumentException(
                "Room with number " + room.getRoomNumber() + " already exists");
        }
        roomRepository.save(room);
    }

    /**
     * Update an existing room.
     * @throws IllegalArgumentException if room doesn't exist
     */
    public void updateRoom(Room room) {
        if (!roomRepository.existsById(room.getRoomNumber())) {
            throw new IllegalArgumentException(
                "Room with number " + room.getRoomNumber() + " not found");
        }
        roomRepository.save(room);
    }

    /**
     * Delete a room by room number.
     * @return true if deleted, false if not found
     */
    public boolean deleteRoom(String roomNumber) {
        return roomRepository.delete(roomNumber);
    }

    /**
     * Get all available rooms.
     */
    public List<Room> getAvailableRooms() {
        return roomRepository.findAll().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Get available rooms of a specific type.
     */
    public List<Room> getAvailableRoomsByType(RoomType type) {
        return roomRepository.findAll().stream()
                .filter(Room::isAvailable)
                .filter(room -> room.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Search rooms by room number (partial match).
     */
    public List<Room> searchByRoomNumber(String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase();
        return roomRepository.findAll().stream()
                .filter(room -> room.getRoomNumber().toLowerCase().contains(lowerSearch))
                .collect(Collectors.toList());
    }

    /**
     * Set room availability.
     */
    public void setAvailability(String roomNumber, boolean available) {
        Room room = roomRepository.findById(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Room " + roomNumber + " not found"));
        room.setAvailable(available);
        roomRepository.save(room);
    }

    /**
     * Get total room count.
     */
    public long getTotalRoomCount() {
        return roomRepository.count();
    }

    /**
     * Get available room count.
     */
    public long getAvailableRoomCount() {
        return roomRepository.findAll().stream()
                .filter(Room::isAvailable)
                .count();
    }
}
