package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Booking;
import com.example.hotel.model.Guest;
import com.example.hotel.model.Room;
import com.example.hotel.persistence.Settings;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.GuestService;
import com.example.hotel.util.MoneyUtil;
import com.example.hotel.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Controller for the Guest Booking Form screen.
 * Collects guest information and confirms the booking.
 */
public class GuestBookingFormController {

    @FXML
    private Label roomInfoLabel;

    @FXML
    private Label datesLabel;

    @FXML
    private Label nightsLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label vatLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label lookupLabel;

    private final GuestService guestService;
    private final BookingService bookingService;
    private final Settings settings;

    public GuestBookingFormController() {
        this.guestService = new GuestService();
        this.bookingService = new BookingService();
        this.settings = Settings.getInstance();
    }

    @FXML
    public void initialize() {
        Room room = GuestPortalState.getSelectedRoom();
        LocalDate checkIn = GuestPortalState.getCheckInDate();
        LocalDate checkOut = GuestPortalState.getCheckOutDate();

        if (room == null || checkIn == null || checkOut == null) {
            errorLabel.setText("Missing booking information. Please start over.");
            return;
        }

        // Display room and booking info
        roomInfoLabel.setText("Room " + room.getRoomNumber() + " - " + room.getType());
        datesLabel.setText(checkIn + " to " + checkOut);

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        nightsLabel.setText(nights + " night(s)");

        double subtotal = bookingService.calculateSubtotal(room, checkIn, checkOut);
        double total = bookingService.calculateTotalCost(room, checkIn, checkOut);
        double vat = total - subtotal;

        subtotalLabel.setText(MoneyUtil.formatCurrency(subtotal));
        vatLabel.setText(MoneyUtil.formatCurrency(vat) + " (" + (int)(settings.getVatRate() * 100) + "%)");
        totalLabel.setText(MoneyUtil.formatCurrency(total));

        errorLabel.setText("");
        lookupLabel.setText("");

        // Add email lookup on focus lost
        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !emailField.getText().isEmpty()) {
                lookupGuest();
            }
        });
    }

    private void lookupGuest() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !ValidationUtil.isValidEmail(email)) {
            return;
        }

        guestService.findByEmail(email).ifPresent(guest -> {
            nameField.setText(guest.getName());
            phoneField.setText(guest.getPhone());
            lookupLabel.setText("Welcome back, " + guest.getName() + "!");
            lookupLabel.setStyle("-fx-text-fill: #4CAF50;");
        });
    }

    @FXML
    private void handleLookupGuest() {
        lookupGuest();
    }

    @FXML
    private void handleConfirmBooking() {
        errorLabel.setText("");

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        // Validate inputs
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            errorLabel.setText("All fields are required");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            errorLabel.setText("Please enter a valid email address");
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            errorLabel.setText("Please enter a valid phone number");
            return;
        }

        try {
            // Find or create guest
            Guest guest = guestService.findOrCreateGuest(name, phone, email);

            // Create booking
            Room room = GuestPortalState.getSelectedRoom();
            LocalDate checkIn = GuestPortalState.getCheckInDate();
            LocalDate checkOut = GuestPortalState.getCheckOutDate();

            Booking booking = bookingService.createBooking(
                guest.getId(),
                room.getRoomNumber(),
                checkIn,
                checkOut
            );

            GuestPortalState.setCreatedBooking(booking);
            App.showGuestBookingConfirmation();

        } catch (IllegalArgumentException e) {
            errorLabel.setText("Booking failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        App.showGuestRoomBrowser();
    }
}
