package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Booking;
import com.example.hotel.model.Room;
import com.example.hotel.persistence.Settings;
import com.example.hotel.util.MoneyUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the Guest Booking Confirmation screen.
 * Displays booking success and details.
 */
public class GuestBookingConfirmationController {

    @FXML
    private Label confirmationLabel;

    @FXML
    private Label bookingIdLabel;

    @FXML
    private Label roomLabel;

    @FXML
    private Label datesLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label thankYouLabel;

    private final Settings settings;

    public GuestBookingConfirmationController() {
        this.settings = Settings.getInstance();
    }

    @FXML
    public void initialize() {
        Booking booking = GuestPortalState.getCreatedBooking();
        Room room = GuestPortalState.getSelectedRoom();

        if (booking == null) {
            confirmationLabel.setText("Error: No booking found");
            return;
        }

        confirmationLabel.setText("Booking Confirmed!");
        bookingIdLabel.setText("Confirmation #: " + booking.getBookingId());
        roomLabel.setText("Room " + booking.getRoomNumber() +
            (room != null ? " - " + room.getType() : ""));
        datesLabel.setText(booking.getCheckInDate() + " to " + booking.getCheckOutDate());
        totalLabel.setText("Total: " + MoneyUtil.formatCurrency(booking.getTotalCost()));
        thankYouLabel.setText("Thank you for booking with " + settings.getHotelName() + "!");
    }

    @FXML
    private void handleDone() {
        GuestPortalState.clear();
        App.showGuestPortalHome();
    }

    @FXML
    private void handleNewBooking() {
        GuestPortalState.clear();
        App.showGuestRoomBrowser();
    }
}
