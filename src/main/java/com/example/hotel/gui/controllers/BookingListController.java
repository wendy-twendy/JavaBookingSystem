package com.example.hotel.gui.controllers;

import com.example.hotel.App;
import com.example.hotel.model.Booking;
import com.example.hotel.model.Guest;
import com.example.hotel.model.enums.BookingStatus;
import com.example.hotel.persistence.Settings;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.GuestService;
import com.example.hotel.service.InvoiceService;
import com.example.hotel.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Booking List view.
 * Handles viewing, filtering, cancelling, and completing bookings.
 */
public class BookingListController {

    @FXML
    private TableView<Booking> bookingTable;

    @FXML
    private TableColumn<Booking, String> colBookingId;

    @FXML
    private TableColumn<Booking, String> colGuestName;

    @FXML
    private TableColumn<Booking, String> colRoom;

    @FXML
    private TableColumn<Booking, String> colCheckIn;

    @FXML
    private TableColumn<Booking, String> colCheckOut;

    @FXML
    private TableColumn<Booking, String> colTotal;

    @FXML
    private TableColumn<Booking, String> colStatus;

    @FXML
    private TableColumn<Booking, String> colRefund;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Button viewInvoiceBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button completeBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label bookingCountLabel;

    private final BookingService bookingService;
    private final GuestService guestService;
    private final InvoiceService invoiceService;
    private final Settings settings;

    private final ObservableList<Booking> bookingList;

    // Static field for passing booking to Invoice view
    private static Booking selectedBookingForInvoice;

    public BookingListController() {
        this.bookingService = new BookingService();
        this.guestService = new GuestService();
        this.invoiceService = new InvoiceService();
        this.settings = Settings.getInstance();
        this.bookingList = FXCollections.observableArrayList();
    }

    /**
     * Get the selected booking for invoice view.
     */
    public static Booking getSelectedBookingForInvoice() {
        return selectedBookingForInvoice;
    }

    /**
     * Clear the selected booking for invoice.
     */
    public static void clearSelectedBookingForInvoice() {
        selectedBookingForInvoice = null;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupStatusFilter();
        setupTableSelection();
        loadBookings();
    }

    private void setupTableColumns() {
        colBookingId.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBookingId()));

        colGuestName.setCellValueFactory(data -> {
            String guestId = data.getValue().getGuestId();
            return guestService.findById(guestId)
                    .map(Guest::getName)
                    .map(SimpleStringProperty::new)
                    .orElse(new SimpleStringProperty("Unknown"));
        });

        colRoom.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRoomNumber()));

        colCheckIn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCheckInDate().toString()));

        colCheckOut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCheckOutDate().toString()));

        colTotal.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%s %.2f",
                        settings.getCurrency(), data.getValue().getTotalCost())));

        // Status column with color coding
        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().name()));

        colStatus.setCellFactory(column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "CONFIRMED":
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                            break;
                        case "COMPLETED":
                            setStyle("-fx-text-fill: #666666; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        colRefund.setCellValueFactory(data -> {
            double refund = data.getValue().getRefundAmount();
            if (refund > 0) {
                return new SimpleStringProperty(String.format("%s %.2f",
                        settings.getCurrency(), refund));
            }
            return new SimpleStringProperty("-");
        });

        bookingTable.setItems(bookingList);
    }

    private void setupStatusFilter() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Confirmed", "Cancelled", "Completed"));
        statusFilter.setValue("All");

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterBookings();
        });
    }

    private void setupTableSelection() {
        bookingTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    updateButtonStates(newSelection);
                }
        );
    }

    private void updateButtonStates(Booking selected) {
        if (selected == null) {
            viewInvoiceBtn.setDisable(true);
            cancelBtn.setDisable(true);
            completeBtn.setDisable(true);
        } else {
            viewInvoiceBtn.setDisable(false);

            // Can only cancel confirmed bookings
            cancelBtn.setDisable(selected.getStatus() != BookingStatus.CONFIRMED);

            // Can only complete confirmed bookings
            completeBtn.setDisable(selected.getStatus() != BookingStatus.CONFIRMED);
        }
    }

    private void loadBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            bookingList.setAll(bookings);
            updateBookingCount();
            updateStatus("Loaded " + bookings.size() + " bookings");
        } catch (Exception e) {
            updateStatus("Error loading bookings: " + e.getMessage());
            AlertUtil.showError("Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    private void filterBookings() {
        String filter = statusFilter.getValue();

        try {
            List<Booking> allBookings = bookingService.getAllBookings();
            List<Booking> filtered;

            if ("All".equals(filter) || filter == null) {
                filtered = allBookings;
            } else {
                BookingStatus status = BookingStatus.valueOf(filter.toUpperCase());
                filtered = allBookings.stream()
                        .filter(b -> b.getStatus() == status)
                        .collect(Collectors.toList());
            }

            bookingList.setAll(filtered);
            updateBookingCount();
            updateStatus("Showing " + filtered.size() + " bookings");
        } catch (Exception e) {
            updateStatus("Error filtering bookings: " + e.getMessage());
        }
    }

    private void updateBookingCount() {
        bookingCountLabel.setText(bookingList.size() + " booking(s)");
    }

    @FXML
    private void handleViewInvoice() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a booking to view invoice.");
            return;
        }

        selectedBookingForInvoice = selected;
        updateStatus("Opening invoice for booking " + selected.getBookingId() + "...");
        App.showInvoice();
    }

    @FXML
    private void handleCancelBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a booking to cancel.");
            return;
        }

        if (selected.getStatus() != BookingStatus.CONFIRMED) {
            AlertUtil.showWarning("Cannot Cancel", "Only confirmed bookings can be cancelled.");
            return;
        }

        // Get guest name for confirmation message
        String guestName = guestService.findById(selected.getGuestId())
                .map(Guest::getName)
                .orElse("Unknown");

        boolean confirmed = AlertUtil.showConfirmation(
                "Confirm Cancellation",
                String.format("Cancel booking %s for %s?\n\nRoom: %s\nCheck-in: %s\nCheck-out: %s\n\nRefund will be calculated based on the refund policy.",
                        selected.getBookingId(),
                        guestName,
                        selected.getRoomNumber(),
                        selected.getCheckInDate(),
                        selected.getCheckOutDate())
        );

        if (confirmed) {
            try {
                Booking cancelled = bookingService.cancelBooking(selected.getBookingId());
                double refund = cancelled.getRefundAmount();

                String refundMessage = refund > 0
                        ? String.format("Refund amount: %s %.2f", settings.getCurrency(), refund)
                        : "No refund (based on refund policy)";

                AlertUtil.showSuccess(
                        "Booking cancelled successfully.\n\n" + refundMessage
                );

                filterBookings(); // Refresh with current filter
                updateStatus("Booking " + selected.getBookingId() + " cancelled");
            } catch (IllegalArgumentException e) {
                AlertUtil.showError("Cancellation Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCompleteBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a booking to complete.");
            return;
        }

        if (selected.getStatus() != BookingStatus.CONFIRMED) {
            AlertUtil.showWarning("Cannot Complete", "Only confirmed bookings can be completed.");
            return;
        }

        String guestName = guestService.findById(selected.getGuestId())
                .map(Guest::getName)
                .orElse("Unknown");

        boolean confirmed = AlertUtil.showConfirmation(
                "Confirm Completion",
                String.format("Mark booking %s for %s as completed?\n\nThis indicates the guest has checked out.",
                        selected.getBookingId(), guestName)
        );

        if (confirmed) {
            try {
                bookingService.completeBooking(selected.getBookingId());
                AlertUtil.showSuccess("Booking marked as completed.");
                filterBookings(); // Refresh with current filter
                updateStatus("Booking " + selected.getBookingId() + " completed");
            } catch (IllegalArgumentException e) {
                AlertUtil.showError("Completion Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleNewBooking() {
        updateStatus("Opening New Booking...");
        App.showBooking();
    }

    @FXML
    private void handleRefresh() {
        filterBookings();
    }

    @FXML
    private void handleBack() {
        App.showDashboard();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
}
