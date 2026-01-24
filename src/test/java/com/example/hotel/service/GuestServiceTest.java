package com.example.hotel.service;

import com.example.hotel.model.Guest;
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
 * Unit tests for GuestService.
 */
class GuestServiceTest {

    private GuestService guestService;
    private FileRepository<Guest, String> guestRepository;

    @BeforeEach
    void setUp() {
        RepositoryFactory.getInstance().refreshAll();
        guestRepository = RepositoryFactory.getInstance().getGuestRepository();
        guestService = new GuestService(guestRepository);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        guestRepository.delete("TEST-G001");
        guestRepository.delete("TEST-G002");
        // Also clean up any auto-generated IDs (they start with G-)
        List<Guest> allGuests = guestRepository.findAll();
        for (Guest guest : allGuests) {
            if (guest.getEmail() != null && guest.getEmail().contains("test@")) {
                guestRepository.delete(guest.getId());
            }
        }
    }

    @Test
    @DisplayName("Should add guest with auto-generated ID")
    void testAddGuestWithAutoId() {
        Guest guest = guestService.addGuest("John Doe", "5551234567", "john.test@example.com");

        assertNotNull(guest.getId(), "Guest ID should be auto-generated");
        assertTrue(guest.getId().startsWith("G-"), "Generated ID should start with 'G-'");
        assertEquals("John Doe", guest.getName());
        assertEquals("5551234567", guest.getPhone());
        assertEquals("john.test@example.com", guest.getEmail());

        // Verify it's persisted
        Optional<Guest> found = guestService.findById(guest.getId());
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should add guest with provided ID")
    void testAddGuestWithId() {
        Guest guest = new Guest("TEST-G001", "Jane Doe", "5559876543", "jane.test@example.com");

        guestService.addGuest(guest);

        Optional<Guest> found = guestService.findById("TEST-G001");
        assertTrue(found.isPresent(), "Guest should be found with provided ID");
        assertEquals("Jane Doe", found.get().getName());
    }

    @Test
    @DisplayName("Should throw exception when adding guest with duplicate ID")
    void testAddDuplicateGuest() {
        Guest guest1 = new Guest("TEST-G001", "John Doe", "5551234567", "john.test@example.com");
        Guest guest2 = new Guest("TEST-G001", "Jane Doe", "5559876543", "jane.test@example.com");

        guestService.addGuest(guest1);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> guestService.addGuest(guest2)
        );
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should generate ID when adding guest with null ID")
    void testAddGuestWithNullId() {
        Guest guest = new Guest(null, "Auto ID Guest", "5551111111", "auto.test@example.com");

        guestService.addGuest(guest);

        assertNotNull(guest.getId(), "ID should be generated for null ID");
        assertTrue(guest.getId().startsWith("G-"));
    }

    @Test
    @DisplayName("Should generate ID when adding guest with empty ID")
    void testAddGuestWithEmptyId() {
        Guest guest = new Guest("", "Empty ID Guest", "5552222222", "empty.test@example.com");

        guestService.addGuest(guest);

        assertNotNull(guest.getId());
        assertFalse(guest.getId().isEmpty(), "ID should not be empty");
        assertTrue(guest.getId().startsWith("G-"));
    }

    @Test
    @DisplayName("Should update existing guest")
    void testUpdateGuest() {
        Guest guest = new Guest("TEST-G001", "Original Name", "5551234567", "original.test@example.com");
        guestService.addGuest(guest);

        guest.setName("Updated Name");
        guest.setEmail("updated.test@example.com");
        guestService.updateGuest(guest);

        Optional<Guest> updated = guestService.findById("TEST-G001");
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("updated.test@example.com", updated.get().getEmail());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent guest")
    void testUpdateNonExistentGuest() {
        Guest guest = new Guest("TEST-NONEXISTENT", "Fake Guest", "5550000000", "fake@example.com");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> guestService.updateGuest(guest)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should search guests by name case-insensitively")
    void testSearchByName() {
        Guest guest1 = new Guest("TEST-G001", "John Smith", "5551234567", "john.test@example.com");
        Guest guest2 = new Guest("TEST-G002", "Jane Johnson", "5559876543", "jane.test@example.com");
        guestService.addGuest(guest1);
        guestService.addGuest(guest2);

        // Search for "john" - should match both (John and Johnson)
        List<Guest> results = guestService.searchByName("john");
        assertTrue(results.size() >= 2, "Should find guests with 'john' in name");

        // Case-insensitive search
        List<Guest> upperCaseResults = guestService.searchByName("JOHN");
        assertEquals(results.size(), upperCaseResults.size(),
            "Search should be case-insensitive");

        // Search for "Smith" - should find only John Smith
        List<Guest> smithResults = guestService.searchByName("Smith");
        assertTrue(smithResults.stream()
            .anyMatch(g -> g.getName().equals("John Smith")));
    }

    @Test
    @DisplayName("Should find guest by email")
    void testFindByEmail() {
        Guest guest = new Guest("TEST-G001", "Email Test", "5551234567", "unique.test@example.com");
        guestService.addGuest(guest);

        Optional<Guest> found = guestService.findByEmail("unique.test@example.com");
        assertTrue(found.isPresent(), "Should find guest by email");
        assertEquals("TEST-G001", found.get().getId());
    }

    @Test
    @DisplayName("Should find guest by email case-insensitively")
    void testFindByEmailCaseInsensitive() {
        Guest guest = new Guest("TEST-G001", "Case Test", "5551234567", "CaseTest@Example.COM");
        guestService.addGuest(guest);

        Optional<Guest> found = guestService.findByEmail("casetest@example.com");
        assertTrue(found.isPresent(), "Email search should be case-insensitive");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmailNotFound() {
        Optional<Guest> found = guestService.findByEmail("nonexistent@example.com");
        assertFalse(found.isPresent(), "Should return empty for non-existent email");
    }

    @Test
    @DisplayName("Should find guest by phone number")
    void testFindByPhone() {
        Guest guest = new Guest("TEST-G001", "Phone Test", "5559999999", "phone.test@example.com");
        guestService.addGuest(guest);

        Optional<Guest> found = guestService.findByPhone("5559999999");
        assertTrue(found.isPresent(), "Should find guest by phone");
        assertEquals("TEST-G001", found.get().getId());
    }

    @Test
    @DisplayName("Should delete guest successfully")
    void testDeleteGuest() {
        Guest guest = new Guest("TEST-G001", "Delete Test", "5551234567", "delete.test@example.com");
        guestService.addGuest(guest);

        boolean deleted = guestService.deleteGuest("TEST-G001");

        assertTrue(deleted, "Delete should return true");
        assertFalse(guestService.findById("TEST-G001").isPresent(),
            "Guest should not be found after deletion");
    }

    @Test
    @DisplayName("Should return correct guest count")
    void testGetGuestCount() {
        long initialCount = guestService.getGuestCount();

        guestService.addGuest(new Guest("TEST-G001", "Count Test 1", "5551111111", "count1.test@example.com"));
        guestService.addGuest(new Guest("TEST-G002", "Count Test 2", "5552222222", "count2.test@example.com"));

        long newCount = guestService.getGuestCount();
        assertEquals(initialCount + 2, newCount, "Guest count should increase by 2");
    }
}
