package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.persistence.Settings;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.GuestService;
import com.example.hotel.service.RoomService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the Dashboard view.
 * Displays statistics and provides navigation to other screens.
 */
public class DashboardController {

    @FXML
    private Label hotelNameLabel;

    @FXML
    private Label totalRoomsLabel;

    @FXML
    private Label availableRoomsLabel;

    @FXML
    private Label activeBookingsLabel;

    @FXML
    private Label totalGuestsLabel;

    @FXML
    private Label statusLabel;

    private final RoomService roomService;
    private final BookingService bookingService;
    private final GuestService guestService;
    private final Settings settings;

    public DashboardController() {
        this.roomService = new RoomService();
        this.bookingService = new BookingService();
        this.guestService = new GuestService();
        this.settings = Settings.getInstance();
    }

    @FXML
    public void initialize() {
        // Set hotel name from settings
        hotelNameLabel.setText(settings.getHotelName());

        // Load initial statistics
        refreshStatistics();
    }

    /**
     * Refreshes all statistics displayed on the dashboard.
     */
    @FXML
    public void refreshStatistics() {
        try {
            long totalRooms = roomService.getTotalRoomCount();
            long availableRooms = roomService.getAvailableRoomCount();
            long activeBookings = bookingService.getActiveBookingCount();
            long totalGuests = guestService.getGuestCount();

            totalRoomsLabel.setText(String.valueOf(totalRooms));
            availableRoomsLabel.setText(String.valueOf(availableRooms));
            activeBookingsLabel.setText(String.valueOf(activeBookings));
            totalGuestsLabel.setText(String.valueOf(totalGuests));

            updateStatus("Statistics refreshed");
        } catch (Exception e) {
            updateStatus("Error loading statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageRooms() {
        updateStatus("Opening Room Management...");
        App.showRoomManagement();
    }

    @FXML
    private void handleManageGuests() {
        updateStatus("Opening Guest Management...");
        App.showGuestManagement();
    }

    @FXML
    private void handleNewBooking() {
        updateStatus("Opening New Booking...");
        App.showBooking();
    }

    @FXML
    private void handleViewBookings() {
        updateStatus("Opening Booking List...");
        App.showBookingList();
    }

    @FXML
    private void handleRefresh() {
        refreshStatistics();
    }

    @FXML
    private void handleBack() {
        App.showPortalSelection();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
}
