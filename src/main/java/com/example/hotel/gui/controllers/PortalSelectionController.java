package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.persistence.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the Portal Selection screen.
 * Entry point for the application - allows choosing between Guest and Staff portals.
 */
public class PortalSelectionController {

    @FXML
    private Label hotelNameLabel;

    private final Settings settings;

    public PortalSelectionController() {
        this.settings = Settings.getInstance();
    }

    @FXML
    public void initialize() {
        hotelNameLabel.setText(settings.getHotelName());
    }

    @FXML
    private void handleGuestPortal() {
        App.showGuestPortalHome();
    }

    @FXML
    private void handleStaffPortal() {
        App.showDashboard();
    }
}
