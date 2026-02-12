package com.example.hotel.service;

import com.example.hotel.model.Booking;
import com.example.hotel.model.Invoice;
import com.example.hotel.model.Room;
import com.example.hotel.persistence.FileRepository;
import com.example.hotel.persistence.RepositoryFactory;
import com.example.hotel.persistence.Settings;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for generating and managing invoices.
 */
public class InvoiceService extends AbstractService<Invoice> {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final Settings settings;

    public InvoiceService() {
        super(RepositoryFactory.getInstance().getInvoiceRepository());
        this.bookingService = new BookingService();
        this.roomService = new RoomService();
        this.settings = Settings.getInstance();
    }

    // Constructor for testing
    public InvoiceService(FileRepository<Invoice, String> repository,
                          BookingService bookingService,
                          RoomService roomService,
                          Settings settings) {
        super(repository);
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.settings = settings;
    }

    /**
     * Get all invoices.
     */
    public List<Invoice> getAllInvoices() {
        return getAll();
    }

    /**
     * Find invoice by booking ID.
     */
    public Optional<Invoice> findByBookingId(String bookingId) {
        return repository.findAll().stream()
                .filter(inv -> inv.getBookingId().equals(bookingId))
                .findFirst();
    }

    /**
     * Generate an invoice for a booking.
     * If invoice already exists for booking, returns existing invoice.
     */
    public Invoice generateInvoice(String bookingId) {
        // Check if invoice already exists
        Optional<Invoice> existing = findByBookingId(bookingId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Get booking
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Booking " + bookingId + " not found"));

        // Get room for price calculation
        Room room = roomService.findByRoomNumber(booking.getRoomNumber())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Room " + booking.getRoomNumber() + " not found"));

        // Calculate invoice amounts
        long nights = ChronoUnit.DAYS.between(
            booking.getCheckInDate(), booking.getCheckOutDate());
        double subtotal = nights * room.getPricePerNight();
        double vatRate = settings.getVatRate();
        double vat = subtotal * vatRate;
        double total = subtotal + vat;

        // Create invoice
        Invoice invoice = new Invoice(
            generateInvoiceId(),
            bookingId,
            subtotal,
            vatRate,
            vat,
            total
        );

        // Set refund if booking was cancelled
        invoice.setRefundAmount(booking.getRefundAmount());

        repository.save(invoice);
        return invoice;
    }

    /**
     * Update invoice with refund amount (after cancellation).
     */
    public Invoice updateWithRefund(String bookingId, double refundAmount) {
        Invoice invoice = findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Invoice for booking " + bookingId + " not found"));

        invoice.setRefundAmount(refundAmount);
        repository.save(invoice);
        return invoice;
    }

    /**
     * Format invoice for display.
     */
    public String formatInvoice(Invoice invoice) {
        Booking booking = bookingService.findById(invoice.getBookingId())
                .orElse(null);

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("              INVOICE\n");
        sb.append("========================================\n");
        sb.append(String.format("Invoice #:    %s%n", invoice.getInvoiceId()));
        sb.append(String.format("Booking #:    %s%n", invoice.getBookingId()));
        sb.append(String.format("Generated:    %s%n", invoice.getGeneratedAt()));
        sb.append("----------------------------------------\n");

        if (booking != null) {
            sb.append(String.format("Room:         %s%n", booking.getRoomNumber()));
            sb.append(String.format("Check-in:     %s%n", booking.getCheckInDate()));
            sb.append(String.format("Check-out:    %s%n", booking.getCheckOutDate()));
            sb.append(String.format("Nights:       %d%n", booking.getNumberOfNights()));
            sb.append(String.format("Status:       %s%n", booking.getStatus()));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal:     %s %.2f%n",
            settings.getCurrency(), invoice.getSubtotal()));
        sb.append(String.format("VAT (%.0f%%):    %s %.2f%n",
            invoice.getVatRate() * 100, settings.getCurrency(), invoice.getVat()));
        sb.append(String.format("Total:        %s %.2f%n",
            settings.getCurrency(), invoice.getTotal()));

        if (invoice.getRefundAmount() > 0) {
            sb.append("----------------------------------------\n");
            sb.append(String.format("Refund:       %s %.2f%n",
                settings.getCurrency(), invoice.getRefundAmount()));
            sb.append(String.format("Final Amount: %s %.2f%n",
                settings.getCurrency(), invoice.getFinalAmount()));
        }

        sb.append("========================================\n");
        return sb.toString();
    }

    /**
     * Generate a unique invoice ID.
     */
    private String generateInvoiceId() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
