package com.example.hotel.persistence;

import com.example.hotel.model.Booking;
import com.example.hotel.model.Guest;
import com.example.hotel.model.Invoice;
import com.example.hotel.model.Room;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton factory for repository instances.
 */
public final class RepositoryFactory {

    private static final Path DATA_DIR = Paths.get("data");

    private static volatile RepositoryFactory instance;

    private final FileRepository<Room, String> roomRepository;
    private final FileRepository<Guest, String> guestRepository;
    private final FileRepository<Booking, String> bookingRepository;
    private final FileRepository<Invoice, String> invoiceRepository;

    private RepositoryFactory() {
        roomRepository = new FileRepository<>(
                DATA_DIR.resolve("rooms.json"),
                Room::getRoomNumber,
                Room.class
        );
        guestRepository = new FileRepository<>(
                DATA_DIR.resolve("guests.json"),
                Guest::getId,
                Guest.class
        );
        bookingRepository = new FileRepository<>(
                DATA_DIR.resolve("bookings.json"),
                Booking::getBookingId,
                Booking.class
        );
        invoiceRepository = new FileRepository<>(
                DATA_DIR.resolve("invoices.json"),
                Invoice::getInvoiceId,
                Invoice.class
        );
    }

    /**
     * Returns the singleton instance of the factory.
     */
    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    /**
     * Returns the room repository.
     */
    public FileRepository<Room, String> getRoomRepository() {
        return roomRepository;
    }

    /**
     * Returns the guest repository.
     */
    public FileRepository<Guest, String> getGuestRepository() {
        return guestRepository;
    }

    /**
     * Returns the booking repository.
     */
    public FileRepository<Booking, String> getBookingRepository() {
        return bookingRepository;
    }

    /**
     * Returns the invoice repository.
     */
    public FileRepository<Invoice, String> getInvoiceRepository() {
        return invoiceRepository;
    }

    /**
     * Reloads all repositories from their files.
     */
    public void refreshAll() {
        roomRepository.refresh();
        guestRepository.refresh();
        bookingRepository.refresh();
        invoiceRepository.refresh();
    }
}
