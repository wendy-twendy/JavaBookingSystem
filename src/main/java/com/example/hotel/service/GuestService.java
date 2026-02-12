package com.example.hotel.service;

import com.example.hotel.model.Guest;
import com.example.hotel.persistence.FileRepository;
import com.example.hotel.persistence.RepositoryFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing guests.
 * Contains business logic for guest operations.
 */
public class GuestService extends AbstractService<Guest> {

    public GuestService() {
        super(RepositoryFactory.getInstance().getGuestRepository());
    }

    // Constructor for testing with mock repository
    public GuestService(FileRepository<Guest, String> guestRepository) {
        super(guestRepository);
    }

    /**
     * Get all guests.
     */
    public List<Guest> getAllGuests() {
        return getAll();
    }

    /**
     * Add a new guest with auto-generated ID.
     * @return The created guest with assigned ID
     */
    public Guest addGuest(String name, String phone, String email) {
        String id = generateGuestId();
        Guest guest = new Guest(id, name, phone, email);
        repository.save(guest);
        return guest;
    }

    /**
     * Add a guest (with existing ID).
     */
    public void addGuest(Guest guest) {
        if (guest.getId() == null || guest.getId().isEmpty()) {
            guest.setId(generateGuestId());
        }
        if (repository.existsById(guest.getId())) {
            throw new IllegalArgumentException(
                "Guest with ID " + guest.getId() + " already exists");
        }
        repository.save(guest);
    }

    /**
     * Update an existing guest.
     */
    public void updateGuest(Guest guest) {
        if (!repository.existsById(guest.getId())) {
            throw new IllegalArgumentException(
                "Guest with ID " + guest.getId() + " not found");
        }
        repository.save(guest);
    }

    /**
     * Delete a guest by ID.
     */
    public boolean deleteGuest(String guestId) {
        return delete(guestId);
    }

    /**
     * Search guests by name (partial match, case-insensitive).
     */
    public List<Guest> searchByName(String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase();
        return repository.findAll().stream()
                .filter(guest -> guest.getName().toLowerCase().contains(lowerSearch))
                .collect(Collectors.toList());
    }

    /**
     * Find guest by email.
     */
    public Optional<Guest> findByEmail(String email) {
        return repository.findAll().stream()
                .filter(guest -> guest.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Find guest by phone number.
     */
    public Optional<Guest> findByPhone(String phone) {
        return repository.findAll().stream()
                .filter(guest -> guest.getPhone().equals(phone))
                .findFirst();
    }

    /**
     * Get total guest count.
     */
    public long getGuestCount() {
        return count();
    }

    /**
     * Find an existing guest by email or create a new one.
     * Used by the Guest Portal for self-service bookings.
     */
    public Guest findOrCreateGuest(String name, String phone, String email) {
        Optional<Guest> existing = findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        return addGuest(name, phone, email);
    }

    /**
     * Generate a unique guest ID.
     */
    private String generateGuestId() {
        return "G-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
