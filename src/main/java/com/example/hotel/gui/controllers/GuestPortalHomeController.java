package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.persistence.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the Guest Portal Home screen.
 * Entry point for self-service guest bookings.
 */
public class GuestPortalHomeController {

    @FXML
    private Label hotelNameLabel;

    @FXML
    private Label welcomeLabel;

    private final Settings settings;

    public GuestPortalHomeController() {
        this.settings = Settings.getInstance();
    }

    @FXML
    public void initialize() {
        hotelNameLabel.setText(settings.getHotelName());
        welcomeLabel.setText("Welcome to " + settings.getHotelName());
        GuestPortalState.clear();
    }

    @FXML
    private void handleBrowseRooms() {
        App.showGuestRoomBrowser();
    }

    @FXML
    private void handleBack() {
        App.showPortalSelection();
    }
}
