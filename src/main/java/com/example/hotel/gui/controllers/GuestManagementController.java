package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Guest;
import com.example.hotel.service.GuestService;
import com.example.hotel.util.AlertUtil;
import com.example.hotel.util.ValidationUtil;
import com.example.hotel.util.ValidationUtil.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Controller for the Guest Management view.
 * Handles CRUD operations for guests.
 */
public class GuestManagementController {

    @FXML
    private TableView<Guest> guestTable;

    @FXML
    private TableColumn<Guest, String> colGuestId;

    @FXML
    private TableColumn<Guest, String> colName;

    @FXML
    private TableColumn<Guest, String> colPhone;

    @FXML
    private TableColumn<Guest, String> colEmail;

    @FXML
    private TextField guestIdField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

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

    private final GuestService guestService;
    private final ObservableList<Guest> guestList;
    private Guest selectedGuest;

    public GuestManagementController() {
        this.guestService = new GuestService();
        this.guestList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelection();
        loadGuests();
        updateButtonStates();
    }

    private void setupTableColumns() {
        colGuestId.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getId()));

        colName.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getName()));

        colPhone.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPhone()));

        colEmail.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEmail()));

        guestTable.setItems(guestList);
    }

    private void setupTableSelection() {
        guestTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedGuest = newSelection;
                if (newSelection != null) {
                    populateForm(newSelection);
                }
                updateButtonStates();
            }
        );
    }

    private void loadGuests() {
        try {
            List<Guest> guests = guestService.getAllGuests();
            guestList.setAll(guests);
            updateStatus("Loaded " + guests.size() + " guests");
        } catch (Exception e) {
            updateStatus("Error loading guests: " + e.getMessage());
            AlertUtil.showError("Error", "Failed to load guests: " + e.getMessage());
        }
    }

    private void populateForm(Guest guest) {
        guestIdField.setText(guest.getId());
        nameField.setText(guest.getName());
        phoneField.setText(guest.getPhone());
        emailField.setText(guest.getEmail());
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedGuest != null;
        addBtn.setDisable(hasSelection);
        updateBtn.setDisable(!hasSelection);
        deleteBtn.setDisable(!hasSelection);
    }

    @FXML
    private void handleAdd() {
        String name = ValidationUtil.clean(nameField.getText());
        String phone = ValidationUtil.cleanPhone(phoneField.getText());
        String email = ValidationUtil.clean(emailField.getText());

        ValidationResult validation = ValidationUtil.validateGuest(name, phone, email);
        if (!validation.isValid()) {
            AlertUtil.showValidationError(validation.getErrorMessage());
            return;
        }

        try {
            Guest guest = guestService.addGuest(name, phone, email);
            loadGuests();
            handleClear();
            AlertUtil.showSuccess("Guest added successfully with ID: " + guest.getId());
            updateStatus("Guest added: " + guest.getName());
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedGuest == null) {
            AlertUtil.showWarning("No Selection", "Please select a guest to update");
            return;
        }

        String name = ValidationUtil.clean(nameField.getText());
        String phone = ValidationUtil.cleanPhone(phoneField.getText());
        String email = ValidationUtil.clean(emailField.getText());

        ValidationResult validation = ValidationUtil.validateGuest(name, phone, email);
        if (!validation.isValid()) {
            AlertUtil.showValidationError(validation.getErrorMessage());
            return;
        }

        try {
            selectedGuest.setName(name);
            selectedGuest.setPhone(phone);
            selectedGuest.setEmail(email);

            guestService.updateGuest(selectedGuest);
            loadGuests();
            AlertUtil.showSuccess("Guest updated successfully");
            updateStatus("Guest updated: " + name);
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedGuest == null) {
            AlertUtil.showWarning("No Selection", "Please select a guest to delete");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
            "Confirm Delete",
            "Are you sure you want to delete guest " + selectedGuest.getName() + "?"
        );

        if (confirmed) {
            try {
                String guestName = selectedGuest.getName();
                String guestId = selectedGuest.getId();
                boolean deleted = guestService.deleteGuest(guestId);
                if (deleted) {
                    loadGuests();
                    handleClear();
                    AlertUtil.showSuccess("Guest " + guestName + " deleted successfully");
                    updateStatus("Guest deleted: " + guestName);
                } else {
                    AlertUtil.showError("Error", "Failed to delete guest");
                }
            } catch (Exception e) {
                AlertUtil.showError("Error", "Failed to delete guest: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        guestIdField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        guestTable.getSelectionModel().clearSelection();
        selectedGuest = null;
        updateButtonStates();
        updateStatus("Form cleared");
    }

    @FXML
    private void handleSearch() {
        String searchTerm = ValidationUtil.clean(searchField.getText());

        if (searchTerm.isEmpty()) {
            loadGuests();
            return;
        }

        try {
            List<Guest> results = guestService.searchByName(searchTerm);
            guestList.setAll(results);
            updateStatus("Found " + results.size() + " guest(s)");
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
