package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Room;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.RoomService;
import com.example.hotel.util.MoneyUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Guest Room Browser screen.
 * Allows guests to select dates and browse available rooms.
 */
public class GuestRoomBrowserController {

    @FXML
    private DatePicker checkInPicker;

    @FXML
    private DatePicker checkOutPicker;

    @FXML
    private FlowPane roomsContainer;

    @FXML
    private Label messageLabel;

    private final RoomService roomService;
    private final BookingService bookingService;

    public GuestRoomBrowserController() {
        this.roomService = new RoomService();
        this.bookingService = new BookingService();
    }

    @FXML
    public void initialize() {
        // Set default dates
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        // Disable past dates
        checkInPicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        checkOutPicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkIn = checkInPicker.getValue();
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1))
                          || (checkIn != null && !date.isAfter(checkIn)));
            }
        });

        // Update checkout picker when checkin changes
        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && checkOutPicker.getValue() != null
                && !checkOutPicker.getValue().isAfter(newVal)) {
                checkOutPicker.setValue(newVal.plusDays(1));
            }
        });

        // Initial search
        handleSearchRooms();
    }

    @FXML
    private void handleSearchRooms() {
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (checkIn == null || checkOut == null) {
            messageLabel.setText("Please select check-in and check-out dates");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            messageLabel.setText("Check-out must be after check-in");
            return;
        }

        List<Room> availableRooms = roomService.getAvailableRoomsForDates(
            checkIn, checkOut, bookingService);

        roomsContainer.getChildren().clear();

        if (availableRooms.isEmpty()) {
            messageLabel.setText("No rooms available for the selected dates");
        } else {
            messageLabel.setText(availableRooms.size() + " room(s) available");
            for (Room room : availableRooms) {
                roomsContainer.getChildren().add(createRoomCard(room));
            }
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox(10);
        card.getStyleClass().add("room-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));

        Label roomNumber = new Label("Room " + room.getRoomNumber());
        roomNumber.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label roomType = new Label(room.getType().toString());
        roomType.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");

        Label price = new Label(MoneyUtil.formatCurrency(room.getPricePerNight()) + " / night");
        price.setStyle("-fx-font-size: 16px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        Label refundable = new Label(room.isRefundable() ? "Refundable" : "Non-refundable");
        refundable.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (room.isRefundable() ? "#4CAF50" : "#757575") + ";");

        Button bookButton = new Button("Book This Room");
        bookButton.getStyleClass().addAll("button", "button-success");
        bookButton.setOnAction(e -> handleBookRoom(room));

        card.getChildren().addAll(roomNumber, roomType, price, refundable, bookButton);
        return card;
    }

    private void handleBookRoom(Room room) {
        GuestPortalState.setSelectedRoom(room);
        GuestPortalState.setCheckInDate(checkInPicker.getValue());
        GuestPortalState.setCheckOutDate(checkOutPicker.getValue());
        App.showGuestBookingForm();
    }

    @FXML
    private void handleBack() {
        App.showGuestPortalHome();
    }
}
