package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Room;
import com.example.hotel.model.enums.RoomType;
import com.example.hotel.service.RoomService;
import com.example.hotel.util.AlertUtil;
import com.example.hotel.util.MoneyUtil;
import com.example.hotel.util.ValidationUtil;
import com.example.hotel.util.ValidationUtil.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Controller for the Room Management view.
 * Handles CRUD operations for rooms.
 */
public class RoomManagementController {

    @FXML
    private TableView<Room> roomTable;

    @FXML
    private TableColumn<Room, String> colRoomNumber;

    @FXML
    private TableColumn<Room, String> colType;

    @FXML
    private TableColumn<Room, String> colPrice;

    @FXML
    private TableColumn<Room, String> colAvailable;

    @FXML
    private TableColumn<Room, String> colRefundable;

    @FXML
    private TextField roomNumberField;

    @FXML
    private ComboBox<RoomType> roomTypeCombo;

    @FXML
    private TextField priceField;

    @FXML
    private CheckBox availableCheck;

    @FXML
    private CheckBox refundableCheck;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button addBtn;

    @FXML
    private Button updateBtn;

    @FXML
    private Button deleteBtn;

    private final RoomService roomService;
    private final ObservableList<Room> roomList;
    private Room selectedRoom;

    public RoomManagementController() {
        this.roomService = new RoomService();
        this.roomList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBox();
        setupTableSelection();
        loadRooms();
        updateButtonStates();
    }

    private void setupTableColumns() {
        colRoomNumber.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getRoomNumber()));

        colType.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getType().getDisplayName()));

        colPrice.setCellValueFactory(data ->
            new SimpleStringProperty(MoneyUtil.formatCurrency(data.getValue().getPricePerNight())));

        colAvailable.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isAvailable() ? "Yes" : "No"));

        colRefundable.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isRefundable() ? "Yes" : "No"));

        roomTable.setItems(roomList);
    }

    private void setupComboBox() {
        roomTypeCombo.setItems(FXCollections.observableArrayList(RoomType.values()));
        roomTypeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(RoomType type) {
                return type == null ? "" : type.getDisplayName();
            }

            @Override
            public RoomType fromString(String string) {
                return null;
            }
        });
        roomTypeCombo.getSelectionModel().selectFirst();
    }

    private void setupTableSelection() {
        roomTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedRoom = newSelection;
                if (newSelection != null) {
                    populateForm(newSelection);
                }
                updateButtonStates();
            }
        );
    }

    private void loadRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            roomList.setAll(rooms);
            updateStatus("Loaded " + rooms.size() + " rooms");
        } catch (Exception e) {
            updateStatus("Error loading rooms: " + e.getMessage());
            AlertUtil.showError("Error", "Failed to load rooms: " + e.getMessage());
        }
    }

    private void populateForm(Room room) {
        roomNumberField.setText(room.getRoomNumber());
        roomTypeCombo.setValue(room.getType());
        priceField.setText(String.valueOf(room.getPricePerNight()));
        availableCheck.setSelected(room.isAvailable());
        refundableCheck.setSelected(room.isRefundable());
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedRoom != null;
        updateBtn.setDisable(!hasSelection);
        deleteBtn.setDisable(!hasSelection);
        roomNumberField.setEditable(selectedRoom == null);
    }

    @FXML
    private void handleAdd() {
        String roomNumber = ValidationUtil.clean(roomNumberField.getText());
        String priceStr = ValidationUtil.clean(priceField.getText());
        RoomType type = roomTypeCombo.getValue();

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            AlertUtil.showValidationError("Price must be a valid number");
            return;
        }

        ValidationResult validation = ValidationUtil.validateRoom(roomNumber, price);
        if (!validation.isValid()) {
            AlertUtil.showValidationError(validation.getErrorMessage());
            return;
        }

        if (type == null) {
            AlertUtil.showValidationError("Please select a room type");
            return;
        }

        try {
            Room room = new Room(
                roomNumber,
                type,
                price,
                availableCheck.isSelected(),
                refundableCheck.isSelected()
            );
            roomService.addRoom(room);
            loadRooms();
            handleClear();
            AlertUtil.showSuccess("Room " + roomNumber + " added successfully");
            updateStatus("Room added: " + roomNumber);
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedRoom == null) {
            AlertUtil.showWarning("No Selection", "Please select a room to update");
            return;
        }

        String priceStr = ValidationUtil.clean(priceField.getText());
        RoomType type = roomTypeCombo.getValue();

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            AlertUtil.showValidationError("Price must be a valid number");
            return;
        }

        if (!ValidationUtil.isValidPrice(price)) {
            AlertUtil.showValidationError("Price must be a positive number");
            return;
        }

        if (type == null) {
            AlertUtil.showValidationError("Please select a room type");
            return;
        }

        try {
            selectedRoom.setType(type);
            selectedRoom.setPricePerNight(price);
            selectedRoom.setAvailable(availableCheck.isSelected());
            selectedRoom.setRefundable(refundableCheck.isSelected());

            roomService.updateRoom(selectedRoom);
            loadRooms();
            AlertUtil.showSuccess("Room updated successfully");
            updateStatus("Room updated: " + selectedRoom.getRoomNumber());
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedRoom == null) {
            AlertUtil.showWarning("No Selection", "Please select a room to delete");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
            "Confirm Delete",
            "Are you sure you want to delete room " + selectedRoom.getRoomNumber() + "?"
        );

        if (confirmed) {
            try {
                String roomNumber = selectedRoom.getRoomNumber();
                boolean deleted = roomService.deleteRoom(roomNumber);
                if (deleted) {
                    loadRooms();
                    handleClear();
                    AlertUtil.showSuccess("Room " + roomNumber + " deleted successfully");
                    updateStatus("Room deleted: " + roomNumber);
                } else {
                    AlertUtil.showError("Error", "Failed to delete room");
                }
            } catch (Exception e) {
                AlertUtil.showError("Error", "Failed to delete room: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        roomNumberField.clear();
        roomNumberField.setEditable(true);
        roomTypeCombo.getSelectionModel().selectFirst();
        priceField.clear();
        availableCheck.setSelected(true);
        refundableCheck.setSelected(true);
        roomTable.getSelectionModel().clearSelection();
        selectedRoom = null;
        updateButtonStates();
        updateStatus("Form cleared");
    }

    @FXML
    private void handleSearch() {
        String searchTerm = ValidationUtil.clean(searchField.getText());

        if (searchTerm.isEmpty()) {
            loadRooms();
            return;
        }

        try {
            List<Room> results = roomService.searchByRoomNumber(searchTerm);
            roomList.setAll(results);
            updateStatus("Found " + results.size() + " room(s)");
        } catch (Exception e) {
            updateStatus("Search error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        App.showDashboard();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
}
