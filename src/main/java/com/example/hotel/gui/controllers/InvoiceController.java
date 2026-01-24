package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Booking;
import com.example.hotel.model.Guest;
import com.example.hotel.model.Invoice;
import com.example.hotel.model.Room;
import com.example.hotel.model.enums.BookingStatus;
import com.example.hotel.persistence.Settings;
import com.example.hotel.service.GuestService;
import com.example.hotel.service.InvoiceService;
import com.example.hotel.service.RoomService;
import com.example.hotel.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Controller for the Invoice view.
 * Displays invoice details for a selected booking.
 */
public class InvoiceController {

    @FXML
    private Label hotelNameLabel;

    @FXML
    private Label invoiceIdLabel;

    @FXML
    private Label bookingIdLabel;

    @FXML
    private Label invoiceDateLabel;

    @FXML
    private Label bookingStatusLabel;

    @FXML
    private Label guestNameLabel;

    @FXML
    private Label roomLabel;

    @FXML
    private Label checkInLabel;

    @FXML
    private Label checkOutLabel;

    @FXML
    private Label nightsLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label vatRateLabel;

    @FXML
    private Label vatLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private VBox refundSection;

    @FXML
    private Label refundLabel;

    @FXML
    private Label finalAmountLabel;

    private final InvoiceService invoiceService;
    private final GuestService guestService;
    private final RoomService roomService;
    private final Settings settings;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public InvoiceController() {
        this.invoiceService = new InvoiceService();
        this.guestService = new GuestService();
        this.roomService = new RoomService();
        this.settings = Settings.getInstance();
    }

    @FXML
    public void initialize() {
        // Set hotel name from settings
        hotelNameLabel.setText(settings.getHotelName());

        // Update VAT label
        double vatPercent = settings.getVatRate() * 100;
        vatRateLabel.setText(String.format("VAT (%.0f%%):", vatPercent));

        // Load invoice
        loadInvoice();
    }

    private void loadInvoice() {
        Booking booking = BookingListController.getSelectedBookingForInvoice();

        if (booking == null) {
            AlertUtil.showError("Error", "No booking selected for invoice.");
            handleBack();
            return;
        }

        try {
            // Generate or retrieve invoice
            Invoice invoice = invoiceService.generateInvoice(booking.getBookingId());

            // Update invoice with refund if booking was cancelled after invoice was created
            if (booking.getStatus() == BookingStatus.CANCELLED && invoice.getRefundAmount() == 0
                    && booking.getRefundAmount() > 0) {
                invoice = invoiceService.updateWithRefund(booking.getBookingId(), booking.getRefundAmount());
            }

            // Populate invoice info
            invoiceIdLabel.setText(invoice.getInvoiceId());
            bookingIdLabel.setText(booking.getBookingId());
            invoiceDateLabel.setText(invoice.getGeneratedAt().format(DATETIME_FORMATTER));

            // Status with color
            String status = booking.getStatus().name();
            bookingStatusLabel.setText(status);
            switch (booking.getStatus()) {
                case CONFIRMED:
                    bookingStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    break;
                case CANCELLED:
                    bookingStatusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    break;
                case COMPLETED:
                    bookingStatusLabel.setStyle("-fx-text-fill: #666666; -fx-font-weight: bold;");
                    break;
            }

            // Guest info
            String guestName = guestService.findById(booking.getGuestId())
                    .map(Guest::getName)
                    .orElse("Unknown");
            guestNameLabel.setText(guestName);

            // Room info
            String roomInfo = roomService.findByRoomNumber(booking.getRoomNumber())
                    .map(room -> String.format("%s (%s)", room.getRoomNumber(), room.getType().name()))
                    .orElse(booking.getRoomNumber());
            roomLabel.setText(roomInfo);

            // Dates
            checkInLabel.setText(booking.getCheckInDate().format(DATE_FORMATTER));
            checkOutLabel.setText(booking.getCheckOutDate().format(DATE_FORMATTER));
            nightsLabel.setText(String.valueOf(booking.getNumberOfNights()));

            // Cost breakdown
            String currency = settings.getCurrency();
            subtotalLabel.setText(String.format("%s %.2f", currency, invoice.getSubtotal()));
            vatLabel.setText(String.format("%s %.2f", currency, invoice.getVat()));
            totalLabel.setText(String.format("%s %.2f", currency, invoice.getTotal()));

            // Refund section
            if (invoice.getRefundAmount() > 0) {
                refundSection.setVisible(true);
                refundSection.setManaged(true);
                refundLabel.setText(String.format("%s %.2f", currency, invoice.getRefundAmount()));
                finalAmountLabel.setText(String.format("%s %.2f", currency, invoice.getFinalAmount()));
            } else {
                refundSection.setVisible(false);
                refundSection.setManaged(false);
            }

        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load invoice: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrint() {
        AlertUtil.showInfo("Print Invoice",
                "In a production application, this would send the invoice to a printer or generate a PDF.\n\n" +
                        "For this demo, the invoice is displayed on screen.");
    }

    @FXML
    private void handleBack() {
        BookingListController.clearSelectedBookingForInvoice();
        App.showBookingList();
    }
}
