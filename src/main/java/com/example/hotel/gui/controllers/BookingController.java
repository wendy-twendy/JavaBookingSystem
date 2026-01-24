package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Booking;
import com.example.hotel.model.Guest;
import com.example.hotel.model.Room;
import com.example.hotel.persistence.Settings;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.GuestService;
import com.example.hotel.service.RoomService;
import com.example.hotel.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controller for the Booking view.
 * Handles creating new bookings with guest, room, and date selection.
 */
public class BookingController {

    @FXML
    private ComboBox<Guest> guestCombo;

    @FXML
    private ComboBox<Room> roomCombo;

    @FXML
    private DatePicker checkInPicker;

    @FXML
    private DatePicker checkOutPicker;

    @FXML
    private Label roomInfoLabel;

    @FXML
    private Label roomTypeLabel;

    @FXML
    private Label nightsLabel;

    @FXML
    private Label pricePerNightLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label vatRateLabel;

    @FXML
    private Label vatLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label refundPolicyLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button confirmBtn;

    private final GuestService guestService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final Settings settings;

    private boolean costCalculated = false;

    public BookingController() {
        this.guestService = new GuestService();
        this.roomService = new RoomService();
        this.bookingService = new BookingService();
        this.settings = Settings.getInstance();
    }

    @FXML
    public void initialize() {
        setupGuestCombo();
        setupRoomCombo();
        setupDatePickers();
        setupRefundPolicyLabel();
        updateVatLabel();
    }

    private void setupGuestCombo() {
        guestCombo.setConverter(new StringConverter<Guest>() {
            @Override
            public String toString(Guest guest) {
                if (guest == null) return null;
                return guest.getName() + " (" + guest.getEmail() + ")";
            }

            @Override
            public Guest fromString(String string) {
                return null;
            }
        });

        List<Guest> guests = guestService.getAllGuests();
        guestCombo.setItems(FXCollections.observableArrayList(guests));

        guestCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            resetCostCalculation();
        });
    }

    private void setupRoomCombo() {
        roomCombo.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                if (room == null) return null;
                return String.format("Room %s - %s ($%.2f/night)",
                        room.getRoomNumber(),
                        room.getType().name(),
                        room.getPricePerNight());
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });

        loadAvailableRooms();

        roomCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            resetCostCalculation();
            if (newVal != null) {
                roomInfoLabel.setText("Room " + newVal.getRoomNumber());
                roomTypeLabel.setText(newVal.getType().name());
                pricePerNightLabel.setText(String.format("%s %.2f",
                        settings.getCurrency(), newVal.getPricePerNight()));
            }
        });
    }

    private void loadAvailableRooms() {
        List<Room> availableRooms = roomService.getAvailableRooms();
        roomCombo.setItems(FXCollections.observableArrayList(availableRooms));
    }

    private void setupDatePickers() {
        // Disable past dates for check-in
        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Set default check-in to today
        checkInPicker.setValue(LocalDate.now());

        // Update check-out constraints when check-in changes
        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            resetCostCalculation();
            if (newVal != null) {
                // Disable dates before or equal to check-in
                checkOutPicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(empty || !date.isAfter(newVal));
                    }
                });

                // Auto-adjust check-out if it's invalid
                if (checkOutPicker.getValue() != null && !checkOutPicker.getValue().isAfter(newVal)) {
                    checkOutPicker.setValue(newVal.plusDays(1));
                }
            }
        });

        // Set default check-out to tomorrow
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            resetCostCalculation();
        });
    }

    private void setupRefundPolicyLabel() {
        String policy = settings.getDefaultRefundPolicy();
        String policyText;
        switch (policy.toUpperCase()) {
            case "FULL":
                policyText = "Full Refund: 100% refund available at any time before check-in.";
                break;
            case "NONE":
                policyText = "No Refund: This booking is non-refundable.";
                break;
            case "TIERED":
            default:
                policyText = "Tiered: 100% if cancelled 7+ days before check-in, 50% for 3-6 days, 0% for less than 3 days.";
                break;
        }
        refundPolicyLabel.setText(policyText);
    }

    private void updateVatLabel() {
        double vatPercent = settings.getVatRate() * 100;
        vatRateLabel.setText(String.format("VAT (%.0f%%):", vatPercent));
    }

    private void resetCostCalculation() {
        costCalculated = false;
        confirmBtn.setDisable(true);
        nightsLabel.setText("-");
        subtotalLabel.setText("-");
        vatLabel.setText("-");
        totalLabel.setText("-");
    }

    @FXML
    private void handleCalculate() {
        // Validate selections
        Guest selectedGuest = guestCombo.getValue();
        Room selectedRoom = roomCombo.getValue();
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (selectedGuest == null) {
            AlertUtil.showValidationError("Please select a guest.");
            return;
        }

        if (selectedRoom == null) {
            AlertUtil.showValidationError("Please select a room.");
            return;
        }

        if (checkIn == null) {
            AlertUtil.showValidationError("Please select a check-in date.");
            return;
        }

        if (checkOut == null) {
            AlertUtil.showValidationError("Please select a check-out date.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            AlertUtil.showValidationError("Check-out date must be after check-in date.");
            return;
        }

        if (checkIn.isBefore(LocalDate.now())) {
            AlertUtil.showValidationError("Check-in date cannot be in the past.");
            return;
        }

        // Calculate costs
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double subtotal = bookingService.calculateSubtotal(selectedRoom, checkIn, checkOut);
        double total = bookingService.calculateTotalCost(selectedRoom, checkIn, checkOut);
        double vat = total - subtotal;

        // Update display
        nightsLabel.setText(String.valueOf(nights));
        subtotalLabel.setText(String.format("%s %.2f", settings.getCurrency(), subtotal));
        vatLabel.setText(String.format("%s %.2f", settings.getCurrency(), vat));
        totalLabel.setText(String.format("%s %.2f", settings.getCurrency(), total));

        costCalculated = true;
        confirmBtn.setDisable(false);
        updateStatus("Cost calculated. Click 'Confirm Booking' to proceed.");
    }

    @FXML
    private void handleConfirmBooking() {
        if (!costCalculated) {
            AlertUtil.showWarning("Calculate First", "Please calculate the cost before confirming.");
            return;
        }

        Guest selectedGuest = guestCombo.getValue();
        Room selectedRoom = roomCombo.getValue();
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        // Show confirmation dialog
        String message = String.format(
                "Create booking for %s?\n\nRoom: %s\nCheck-in: %s\nCheck-out: %s\nTotal: %s",
                selectedGuest.getName(),
                selectedRoom.getRoomNumber(),
                checkIn,
                checkOut,
                totalLabel.getText()
        );

        boolean confirmed = AlertUtil.showConfirmation("Confirm Booking", message);

        if (confirmed) {
            try {
                Booking booking = bookingService.createBooking(
                        selectedGuest.getId(),
                        selectedRoom.getRoomNumber(),
                        checkIn,
                        checkOut
                );

                AlertUtil.showSuccess("Booking created successfully!\n\nBooking ID: " + booking.getBookingId());
                updateStatus("Booking created: " + booking.getBookingId());

                // Reset form and reload available rooms
                resetForm();
                loadAvailableRooms();

            } catch (IllegalArgumentException e) {
                AlertUtil.showError("Booking Error", e.getMessage());
                updateStatus("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNewGuest() {
        updateStatus("Opening Guest Management...");
        App.showGuestManagement();
    }

    @FXML
    private void handleBack() {
        App.showDashboard();
    }

    private void resetForm() {
        guestCombo.setValue(null);
        roomCombo.setValue(null);
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
        roomInfoLabel.setText("-");
        roomTypeLabel.setText("-");
        pricePerNightLabel.setText("-");
        resetCostCalculation();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
}
